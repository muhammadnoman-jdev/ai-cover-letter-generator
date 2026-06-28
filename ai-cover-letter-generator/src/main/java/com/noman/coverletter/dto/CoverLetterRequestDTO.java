package com.noman.coverletter.dto;

import jakarta.validation.constraints.NotBlank;

public class CoverLetterRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Skills are required")
    private String skills;

    @NotBlank(message = "Experience is required")
    private String experience;

    @NotBlank(message = "Job description is required")
    private String jobDescription;

    @NotBlank(message = "Tone is required")
    private String tone;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }

    @Override
    public String toString() {
        return "CoverLetterRequestDTO [title=" + title + ", jobTitle=" + jobTitle + "]";
    }
}