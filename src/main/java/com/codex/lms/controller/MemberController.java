package com.codex.lms.controller;

import com.codex.lms.model.Member;
import com.codex.lms.model.MemberStatus;
import com.codex.lms.repository.MemberRepository;
import com.codex.lms.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberRepository memberRepository;
    private final LibraryService libraryService;

    public MemberController(MemberRepository memberRepository, LibraryService libraryService) {
        this.memberRepository = memberRepository;
        this.libraryService = libraryService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("members", libraryService.members(q));
        model.addAttribute("query", q);
        return "members/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("statuses", MemberStatus.values());
        return "members/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberRepository.findById(id).orElseThrow());
        model.addAttribute("statuses", MemberStatus.values());
        return "members/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Member member,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", MemberStatus.values());
            return "members/form";
        }
        if (member.getJoinedOn() == null) {
            member.setJoinedOn(LocalDate.now());
        }
        memberRepository.save(member);
        redirectAttributes.addFlashAttribute("success", "Member saved successfully.");
        return "redirect:/members";
    }
}
