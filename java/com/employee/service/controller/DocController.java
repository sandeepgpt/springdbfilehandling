package com.employee.service.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.employee.service.entity.Doc;
import com.employee.service.modelservice.DocStorageService;
import com.employee.service.repository.DocRepository;



@Controller
public class DocController {
    @Autowired
	private DocStorageService docStorageService;
    
    @Autowired
    private DocRepository docRepo;
    
	@GetMapping("/")
	public String get(Model model) {
		List<Doc> docs = docStorageService.getFiles();
		model.addAttribute("docs", docs);
		return "doc";
	}
	@GetMapping("/new")
	public String displayProjectForm(Model model) {
	       Doc employee = new Doc();
	      model.addAttribute("employee", employee);
		return "new";
	}
	@PostMapping("/save")
	public String uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
		for(MultipartFile file: files) {
			docStorageService.saveFile(file);
		}
		return "redirect:/";
	}
	
	@GetMapping("/downloadFile/{fileId}")
	public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Integer fileId) {
	  try {
	    Optional<Doc> docOptional = docStorageService.getFile(fileId);
	    if (docOptional.isPresent()) {
	      Doc doc = docOptional.get();
	      byte[] data = doc.getData();
	      if (data != null && data.length > 0) {
	        return ResponseEntity.ok()
	            .contentType(MediaType.parseMediaType(doc.getDocType()))
	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getDocName() + "\"")
	            .body(new ByteArrayResource(data));
	      } else {
	        // Handle missing data case
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	      }
	    } else {
	      // Handle file not found case
	      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    }
	  } catch (Exception e) {
	    // Log the exception and return a generic error response
	   // log.error("Error downloading file", e);
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	  }
	}
	
	
	@GetMapping("/updateFile/{fileId}")
	public String updateFile(@PathVariable Integer fileId, Model model) {
		Optional<Doc> docOptional = docStorageService.getFile(fileId);
		if(docOptional.isPresent()) {
			Doc doc = docOptional.get();
			model.addAttribute("doc", doc);
			return "update";   //Assuming you have update template "return "update";"
		}
		else {
			//Handle file not found scenario
			return "error";
		}
   }
	
	
	@PostMapping("/update/{id}")            
    public String update(@PathVariable Integer id, @ModelAttribute Doc doc, @RequestParam(required = false) MultipartFile file) throws IOException {
        Optional<Doc> existingDoc = docRepo.findById(id);
        if (existingDoc.isPresent()) {
            Doc updatedDoc = existingDoc.get();
            updatedDoc.setDocName(doc.getDocName());
            updatedDoc.setDocType(doc.getDocType());

            if (file != null && !file.isEmpty()) {
                // Validate file size, content type, etc. (implement security measures)
                updatedDoc.setData(file.getBytes());
            }

            docRepo.save(updatedDoc);
           return "redirect:/"; // Handle successful update (redirect to success page, etc.)
        } else {
        	return "error";  // Handle file not found scenario
        }
    }


	
	@GetMapping("/deleteFile/{fileId}")
	public String deleteFile(@PathVariable Integer fileId) throws FileNotFoundException {
	    Optional<Doc> optionalDoc = docRepo.findById(fileId);

	    if (optionalDoc.isPresent()) {
	        Doc doc = optionalDoc.get();
	        docRepo.delete(doc);
	        return "redirect:/";
	    } else {
	        // Handle the case where the document with the specified ID is not found
	        throw new FileNotFoundException("Document not found with ID: " + fileId);
	        // Alternatively, you can redirect to an error page or use another error handling mechanism
	    }
	}
	
}





































