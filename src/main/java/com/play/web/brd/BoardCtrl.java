package com.play.web.brd;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.play.web.cmm.Util;
import com.play.web.cmm.Util2;
import com.play.web.page.Pagination;
import com.play.web.tx.TxService;

@RestController
public class BoardCtrl {
	static final Logger logger = LoggerFactory.getLogger(BoardCtrl.class);
	@Autowired Util2 util2;
	@Autowired Board brd;
	@Autowired BoardMapper brdMap;
	@Autowired Pagination page;
	@Autowired TxService tx;
	@Autowired Map<String,Object> map;
	@Resource(name="uploadPath")
	private String uploadPath;
	@PostMapping("/boards/")
	public @ResponseBody Map<String,Object> list(@RequestBody Map<String,Object>cast){
		logger.info("\n BoardCtrl :::::::::: {} !!-----","list");
		map.clear();
		Util.log.accept(cast.get("pageNo")+"");
		map.put("pageNumber",Integer.parseInt((String) cast.get("pageNo")));
		map.put("countRow",brdMap.count());
		page.carryOut(map);
		map.clear();
		map.put("board_id",cast.get("board_id"));
		map.put("beginRow",page.getBeginRow());
		map.put("endRow",page.getEndRow());
		List<Board> ls = brdMap.list(map);
		/*map.put("page", page);*/
		map.put("list", ls);
		return map;
	}
	@GetMapping("/boards/{id}/{pageNo}")
	public @ResponseBody Map<String,Object> myList(@PathVariable String id, @PathVariable int pageNo){
		logger.info("\n BoardCtrl :::::::::: {} !!-----","Mylist");
		map.clear();
		brd.setMember_id(id);
		map.put("pageNumber",pageNo);
		map.put("countRow",brdMap.countRetrieve(brd));
		page.carryOut(map);
		map.clear();
		map.put("writer", id);
		map.put("beginRow", page.getBeginRow());
		map.put("endRow", page.getEndRow());
		List<Board> ls = brdMap.listRetrieve(map);
		map.put("page", page);
		map.put("list", ls);
		return map;
	}
	@PostMapping("/boards/create")
	public @ResponseBody Board create(@RequestBody Board b){
		logger.info("\n BoardCtrl :::::::::: {} !!-----","write");
		b.setMsg_date(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		map.clear();
		map.put("brd", b);
		map.put("userid", b.getMember_id());
		tx.write(map);
		return b;
	}
	@GetMapping("/boards/read/{bno}")
	public @ResponseBody Board write(@PathVariable int bno){
		logger.info("\n BoardCtrl :::::::::: {} !!-----","read()");
		return brdMap.read(bno);
	}
	@GetMapping("/boards/delete/{id}/{bno}")
	public void delete(@PathVariable String id, @PathVariable int bno){
		logger.info("\n BoardCtrl :::::::::: {} !!-----","delete()");
		map.clear();
		map.put("bno", bno);
		map.put("userid", id);
		tx.delete(map);
	}
	@PostMapping("/boards/update/")
	public @ResponseBody void update(@RequestBody Board b){
		logger.info("\n BoardCtrl :::::::::: {} !!-----","update()");
		brdMap.update(b);
	}
	@PostMapping("/boards/fileupload")
	public Object fileupload(@ModelAttribute("uploadForm") FileForm uploadForm) throws IOException{
		Util.log.accept(":: BoardCtrl :: fileupload() ");
		 List<MultipartFile> files = uploadForm.getFiles();

		  List<String> fileNames = new ArrayList<String>();
		  if (null != files && files.size() > 0) {
		   for (MultipartFile multipartFile : files) {
		    String fileName = multipartFile.getOriginalFilename();
		    String path = uploadPath + fileName;

		File f = new File(path);

		multipartFile.transferTo(f);

		fileNames.add(fileName);
		Util.log.accept("fileupload SUCCESS !! ");
		   }
		  }
		  //map.addAttribute("files", fileNames);
		  return "success";
	}
    class FileForm {
        private List<MultipartFile> files;
        public List<MultipartFile> getFiles() {
         return files;
        }
        public void setFiles(List<MultipartFile> files) {
         this.files = files;
        }
    }
    @PostMapping("/uploadAjax")
    public ResponseEntity<String> uploadAjax(MultipartFile file) throws Exception{
    	Util.log.accept("originaName: " + file.getOriginalFilename());
    	Util.log.accept("size: " + file.getSize());
    	Util.log.accept("contentType: " + file.getContentType());
		return new ResponseEntity<>(file.getOriginalFilename(),HttpStatus.CREATED);
    }
}
