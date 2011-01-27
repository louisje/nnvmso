package com.nnvmso.web.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Mso;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.TranscodingService;

@Controller
@RequestMapping("admin/config")
public class AdminConfigController {
	@RequestMapping("transcodingServer")
	public ResponseEntity<String> transcodingServer(HttpServletRequest req) {
		TranscodingService tranService = new TranscodingService();
		tranService.getTranscodingEnv(req);
		String[] transcodingEnv = tranService.getTranscodingEnv(req);
		String transcodingServer = transcodingEnv[0];
		String callbackUrl = transcodingEnv[1];
		
		String output = "transcoding server: " + transcodingServer + "\n";
		output = output + "callback server: " + callbackUrl;
		
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping("mso")
	public ResponseEntity<String> mso(HttpServletRequest req) {
		Mso mso = new MsoManager().findMsoViaHttpReq(req);
		String output = "";
		if (mso != null) { output = mso.getName(); }
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping("changeMso")
	public ResponseEntity<String> changeMso(@RequestParam(value="mso",required=false) String mso, HttpServletRequest req, HttpServletResponse resp) {
		CookieHelper.setCookie(resp, CookieHelper.MSO, mso);
		return NnNetUtil.textReturn("OK");		
	}
	
	
}