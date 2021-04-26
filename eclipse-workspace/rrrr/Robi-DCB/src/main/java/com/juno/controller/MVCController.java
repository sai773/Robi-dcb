package com.juno.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MVCController {
	
	@RequestMapping(value="/aoc", method = RequestMethod.GET)
    public ModelAndView enterMsisdn(ModelMap model){
	  ModelAndView modelAndview = new ModelAndView("aoc");
	  return modelAndview;	   
	}
	
	@RequestMapping(value="/login", method = RequestMethod.GET)
    public ModelAndView ccguiLogin(ModelMap model){
	  ModelAndView modelAndview = new ModelAndView("login");
	  return modelAndview;	   
	}
}
