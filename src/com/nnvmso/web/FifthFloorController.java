package com.nnvmso.web;

import java.util.Date;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.nnvmso.lib.*;

/** 
 * temporary controller, just used for routing, move to xml later,  
 */
@Controller
@RequestMapping("5f")
public class FifthFloorController {

	protected static final Logger logger = Logger.getLogger(FifthFloorController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}		
	
	@RequestMapping("")
	public String zooatomics(@RequestParam(value="mso",required=false) String mso, HttpServletRequest req, HttpServletResponse resp, Model model) {		
		CookieHelper.setCookie(resp, CookieHelper.MSO, "5f");
		String now = (new SimpleDateFormat("MM.dd.yyyy")).format(new Date()).toString();
		model.addAttribute("now", now);
		return "player/zooatomics";
	}

	
}