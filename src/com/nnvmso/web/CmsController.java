package com.nnvmso.web;

import java.security.SignatureException;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.nnvmso.lib.AmazonLib;
import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.model.Mso;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.AuthService;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.SessionService;

@Controller
public class CmsController {
	
	protected static final Logger logger = Logger.getLogger(CmsController.class.getName());
	private static MessageSource messageSource = new ClassPathXmlApplicationContext("locale.xml");
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
	}
	
	@RequestMapping("cms/logout")
	public String genericCMSLogout(HttpServletResponse resp) {
		CookieHelper.deleteCookie(resp, CookieHelper.USER);
		return "redirect:/9x9";
	}
	
	@RequestMapping(value = "cms/{cmaTab}", method = RequestMethod.GET)
	public String genericCMSLogin(HttpServletRequest request, @PathVariable("cmaTab") String cmsTab, Model model) throws SignatureException {
		String userToken = CookieHelper.getCookie(request, CookieHelper.USER);
		if (userToken == null) {
			logger.warning("user not login");
			return "redirect:/9x9";
		} else {
			NnUserManager userMngr = new NnUserManager();
			MsoManager msoMngr = new MsoManager();
			
			NnUser user = userMngr.findByToken(userToken);
			if (user == null) {
				logger.warning("user not found");
				return "error/404";
			}
			if (user.getType() == NnUser.TYPE_USER) {
				// generate TCO account
				Mso mso = new Mso(user.getName(), user.getIntro(), user.getEmail(), Mso.TYPE_TCO);
				mso.setTitle("TCO");
				mso.setPreferredLangCode(Mso.LANG_EN);
				mso.setLogoUrl("/images/logo_9x9.png");
				msoMngr.create(mso);
				logger.info("create a tco");
				user.setMsoId(mso.getKey().getId());
				user.setType(NnUser.TYPE_TCO);
				userMngr.save(user);
			}
			if (user.getType() == NnUser.TYPE_TCO) {
				Mso mso = msoMngr.findById(user.getMsoId());
				if (mso == null) {
					logger.warning("mso not found");
					return "error/404";
				} else if (mso.getType() != Mso.TYPE_TCO) {
					logger.warning("invalid mso type");
					return "error/404";
				}
				if (cmsTab.equals("admin")) {
					return "redirect:/cms/channelManagement";
				}
				model.addAttribute("msoLogo", mso.getLogoUrl());
				model.addAttribute("mso", mso);
				model.addAttribute("msoId", mso.getKey().getId());
				model.addAttribute("msoType", mso.getType());
				model.addAttribute("logoutUrl", "/cms/logout");
				if (cmsTab.equals("channelManagement") || cmsTab.equals("channelSetManagement")) {
					String policy = AmazonLib.buildS3Policy("9x9tmp", "public-read", "");
					model.addAttribute("s3Policy", policy);
					model.addAttribute("s3Signature", AmazonLib.calculateRFC2104HMAC(policy));
					model.addAttribute("s3Id", AmazonLib.AWS_ID);
					return "cms/" + cmsTab;
				} else if (cmsTab.equals("directoryManagement") || cmsTab.equals("promotionTools") || cmsTab.equals("setup") || cmsTab.equals("statistics")) {
					return "cms/" + cmsTab;
				} else {
					return "error/404";
				}
			} else {
				logger.warning("invalid mso type");
				return "error/404";
			}
		}
	}
	
	@RequestMapping(value = "{msoName}/admin", method = RequestMethod.GET)
	public String admin(HttpServletRequest request, @PathVariable("msoName") String msoName, Model model) throws SignatureException {
		
		if (msoName.equals("cms"))
			return this.genericCMSLogin(request, "admin", model);
		
		SessionService sessionService = new SessionService(request);
		HttpSession session = sessionService.getSession();
		logger.info("msoName = " + msoName);
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findByName(msoName);
		if (mso == null)
			return "error/404";
		
		Mso sessionMso = (Mso)session.getAttribute("mso");
		if (sessionMso != null && sessionMso.getKey().getId() == mso.getKey().getId()) {
			
			return "redirect:/" + msoName + "/channelManagement";
		} else {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					logger.info(cookie.getName());
					if (cookie.getName().length() > 0 && cookie.getName().compareTo("cms_login_" + msoName) == 0) {
						String[] split = cookie.getValue().split("\\|");
						if (split.length >= 2) {
							model.addAttribute("email", split[0]);
							model.addAttribute("password", split[1]);
						}
					}
				}
			}
			model.addAttribute("msoLogo", mso.getLogoUrl());
			sessionService.removeSession();
			return "cms/login";
		}
	}
	
	@RequestMapping(value = "{msoName}/admin", method = RequestMethod.POST)
	public String login(HttpServletRequest request,
	                    HttpServletResponse response,
	                    Model model,
	                    @RequestParam String email,
	                    @RequestParam String password,
	                    @RequestParam(required = false) Boolean rememberMe,
	                    @PathVariable String msoName) {
		
		logger.info(msoName);
		logger.info("email = " + email);
		logger.info("password = " + password);
		logger.info("rememberMe = " + rememberMe);
		
		SessionService sessionService = new SessionService(request);
		AuthService authService = new AuthService();
		MsoManager msoMngr = new MsoManager();
		NnUserManager userMngr = new NnUserManager();
		Locale locale = request.getLocale();
		
		Mso mso = msoMngr.findByName(msoName);
		if (mso == null)
			return "error/404";
		String msoLogo = mso.getLogoUrl();
		Mso msoAuth = authService.msoAuthenticate(email, password, mso.getKey().getId());
		if (msoAuth == null) {
			logger.info("login failed");
			NnUser user = userMngr.findMsoUser(mso);
			String error;
			if (user.getEmail().equals(email)) {
				error = messageSource.getMessage("cms.warning.invalid_password", null, locale);
			} else {
				error = messageSource.getMessage("cms.warning.invalid_account", null, locale);
			}
			model.addAttribute("email", email);
			model.addAttribute("password", password);
			model.addAttribute("msoLogo", msoLogo);
			model.addAttribute("error", error);
			sessionService.removeSession();
			return "cms/login";
		}
		
		HttpSession session = sessionService.getSession();
		session.setAttribute("mso", msoAuth);
		sessionService.saveSession(session);
		
		// set cookie
		if (rememberMe != null && rememberMe) {
			logger.info("set cookie");
			response.addCookie(new Cookie("cms_login_" + msoName, email + "|" + password));
		} else {
			response.addCookie(new Cookie("cms_login_" + msoName, ""));
		}
		
		return "redirect:/" + msoName + "/channelManagement";
	}
	
	@RequestMapping(value = "{msoName}/logout")
	public String logout(Model model, HttpServletRequest request, @PathVariable String msoName) {
		SessionService sessionService = new SessionService(request);
		sessionService.removeSession();
		return "redirect:/" + msoName + "/admin";
	}
	
	@RequestMapping(value = "{msoName}/{cmsTab}")
	public String management(HttpServletRequest request, @PathVariable String msoName, @PathVariable String cmsTab, Model model) throws SignatureException {
		
		SessionService sessionService = new SessionService(request);
		HttpSession session = sessionService.getSession();
		
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findByName(msoName);
		if (mso == null)
			return "error/404";
		
		Mso sessionMso = (Mso)session.getAttribute("mso");
		if (sessionMso != null && sessionMso.getKey().getId() == mso.getKey().getId()) {
			model.addAttribute("msoLogo", mso.getLogoUrl());
			model.addAttribute("mso", mso);
			model.addAttribute("msoId", mso.getKey().getId());
			model.addAttribute("msoType", mso.getType());
			model.addAttribute("logoutUrl", "/" + msoName + "/logout");
			if (cmsTab.equals("channelManagement") || cmsTab.equals("channelSetManagement")) {
				String policy = AmazonLib.buildS3Policy("9x9tmp", "public-read", "");
				model.addAttribute("s3Policy", policy);
				model.addAttribute("s3Signature", AmazonLib.calculateRFC2104HMAC(policy));
				model.addAttribute("s3Id", AmazonLib.AWS_ID);
				return "cms/" + cmsTab;
			} else if (cmsTab.equals("directoryManagement") || cmsTab.equals("promotionTools") || cmsTab.equals("setup") || cmsTab.equals("statistics")) {
				return "cms/" + cmsTab;
			} else {
				return "error/404";
			}
		} else {
			sessionService.removeSession();
			return "redirect:/" + msoName + "/admin";
		}
	}
}
