package inventory.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import inventory.model.History;
import inventory.model.Paging;
import inventory.service.HistoryService;
import inventory.util.Constant;

@Controller
public class HistoryController {
	
	@Autowired
	private HistoryService historyService;
	
	@GetMapping(value = {"/histoty/list", "/history/list/"})
	public String redirect() {
		return "redirect:/history/list/1";
	}
	
	@GetMapping("/history/list/{page}")
	public String listHistory(@PathVariable("page") int page, Model model, @ModelAttribute("searchForm") History history) {
		Paging paging = new Paging(5);
		paging.setIndexPage(page);
		List<History> histories = historyService.getAllHistory(history, paging);
		Map<String, Object> mapType = new HashMap<>();
		mapType.put(String.valueOf(Constant.TYPE_ALL), "All");
		mapType.put(String.valueOf(Constant.TYPE_GOODS_RECEIPT), "Goods Receipt");
		mapType.put(String.valueOf(Constant.TYPE_GOODS_ISSUES), "Goods Issues");
		model.addAttribute("histories", histories);
		model.addAttribute("mapType", mapType);
		model.addAttribute("pageInfo", paging);
		return "history";
	}
}
