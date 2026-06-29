package com.noman.coverletter.controller;

import com.noman.coverletter.dto.CoverLetterRequestDTO;
import com.noman.coverletter.entity.CoverLetter;
import com.noman.coverletter.service.CoverLetterService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.ByteArrayOutputStream;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/cover-letters")
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    public CoverLetterController(CoverLetterService coverLetterService) {
        this.coverLetterService = coverLetterService;
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("coverLetterRequest", new CoverLetterRequestDTO());
        return "cover-letter-form";
    }

    @PostMapping("/generate")
    public String generate(
            @Valid @ModelAttribute("coverLetterRequest") CoverLetterRequestDTO dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "cover-letter-form";
        }

        CoverLetter coverLetter = coverLetterService.generate(dto);
        return "redirect:/cover-letters/" + coverLetter.getId();
    }

    @GetMapping("/{id}")
    public String viewOne(@PathVariable Long id, Model model) {
        CoverLetter coverLetter = coverLetterService.getByIdForCurrentUser(id);
        model.addAttribute("coverLetter", coverLetter);
        return "cover-letter-view";
    }

    @GetMapping("/history")
    public String history(Model model) {
        List<CoverLetter> letters = coverLetterService.getAllForCurrentUser();
        model.addAttribute("letters", letters);
        return "cover-letter-history";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        coverLetterService.deleteByIdForCurrentUser(id);
        return "redirect:/cover-letters/history";
    }
    
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        CoverLetter coverLetter = coverLetterService.getByIdForCurrentUser(id);

        CoverLetterRequestDTO dto = new CoverLetterRequestDTO();
        dto.setTitle(coverLetter.getTitle());
        dto.setJobTitle(coverLetter.getJobTitle());
        dto.setCompanyName(coverLetter.getCompanyName());
        dto.setSkills(coverLetter.getSkills());
        dto.setExperience(coverLetter.getExperience());
        dto.setJobDescription(coverLetter.getJobDescription());
        dto.setTone(coverLetter.getTone());

        model.addAttribute("coverLetterRequest", dto);
        model.addAttribute("coverLetterId", id);
        return "cover-letter-edit";
    }

    @PostMapping("/edit/{id}")
    public String handleEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("coverLetterRequest") CoverLetterRequestDTO dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("coverLetterId", id);
            return "cover-letter-edit";
        }

        CoverLetter updated = coverLetterService.update(id, dto);
        return "redirect:/cover-letters/" + updated.getId();
    }
    
    //for PDF download
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        CoverLetter coverLetter = coverLetterService.getByIdForCurrentUser(id);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 72, 72, 72, 72);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // ── Fonts ──────────────────────────────────────────
            Font nameFont      = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,    new BaseColor(30, 30, 30));
            Font contactFont   = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL,  new BaseColor(80, 80, 80));
            Font bodyFont      = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL,  new BaseColor(40, 40, 40));
            Font companyFont   = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,    new BaseColor(40, 40, 40));

            // ── Header: Applicant Name ─────────────────────────
            String appName = (coverLetter.getApplicantName() != null
                    && !coverLetter.getApplicantName().isEmpty())
                    ? coverLetter.getApplicantName() : "Applicant";

            Paragraph namePara = new Paragraph(appName, nameFont);
            namePara.setAlignment(Element.ALIGN_CENTER);
            namePara.setSpacingAfter(6);
            document.add(namePara);

            // ── Header: Contact Line ───────────────────────────
            StringBuilder contactLine = new StringBuilder();
            if (coverLetter.getApplicantEmail() != null && !coverLetter.getApplicantEmail().isEmpty())
                contactLine.append(coverLetter.getApplicantEmail());
            if (coverLetter.getApplicantPhone() != null && !coverLetter.getApplicantPhone().isEmpty()) {
                if (contactLine.length() > 0) contactLine.append("   •   ");
                contactLine.append(coverLetter.getApplicantPhone());
            }
            if (coverLetter.getApplicantLinkedin() != null && !coverLetter.getApplicantLinkedin().isEmpty()) {
                if (contactLine.length() > 0) contactLine.append("   •   ");
                contactLine.append(coverLetter.getApplicantLinkedin());
            }

            if (contactLine.length() > 0) {
                Paragraph contactPara = new Paragraph(contactLine.toString(), contactFont);
                contactPara.setAlignment(Element.ALIGN_CENTER);
                contactPara.setSpacingAfter(12);
                document.add(contactPara);
            }

            // ── Horizontal Rule ────────────────────────────────
            PdfContentByte cb = writer.getDirectContent();
            cb.setColorStroke(new BaseColor(63, 81, 181)); // indigo line
            cb.setLineWidth(1.5f);
            float lineY = writer.getVerticalPosition(false);
            cb.moveTo(72, lineY);
            cb.lineTo(document.getPageSize().getWidth() - 72, lineY);
            cb.stroke();

            document.add(new Paragraph(" ")); // spacer after line

            // ── Applying For Section ───────────────────────────
            Paragraph applyingFor = new Paragraph();
            applyingFor.add(new Chunk("Applying for: ", contactFont));
            applyingFor.add(new Chunk(coverLetter.getJobTitle() + " at " + coverLetter.getCompanyName(), companyFont));
            applyingFor.setSpacingAfter(16);
            document.add(applyingFor);

            // ── Cover Letter Body ──────────────────────────────
            String content = coverLetter.getGeneratedContent() != null
                    ? coverLetter.getGeneratedContent().trim() : "";

            // Split by newlines to preserve paragraph spacing
            String[] lines = content.split("\n");
            for (String line : lines) {
                Paragraph para = new Paragraph(line.trim(), bodyFont);
                para.setLeading(20);
                if (line.trim().isEmpty()) {
                    para.setSpacingAfter(8);
                }
                document.add(para);
            }
            
         // Add applicant name after Sincerely
            if (coverLetter.getApplicantName() != null && !coverLetter.getApplicantName().isEmpty()) {
                Font signoffFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(40, 40, 40));
                Paragraph signoff = new Paragraph(coverLetter.getApplicantName(), signoffFont);
                signoff.setSpacingBefore(4);
                document.add(signoff);
            }

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    coverLetter.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/save-edit/{id}")
    public String saveEdit(
            @PathVariable Long id,
            @RequestParam("editedContent") String editedContent,
            @RequestParam("applicantName") String applicantName,
            @RequestParam("applicantEmail") String applicantEmail,
            @RequestParam("applicantPhone") String applicantPhone,
            @RequestParam("applicantLinkedin") String applicantLinkedin) {

        CoverLetter coverLetter = coverLetterService.saveEditedContent(id, editedContent);
        coverLetter = coverLetterService.saveContactInfo(id, applicantName, applicantEmail, applicantPhone, applicantLinkedin);
        return "redirect:/cover-letters/" + coverLetter.getId();
    }
}