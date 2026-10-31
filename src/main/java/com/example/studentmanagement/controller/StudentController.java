package com.example.studentmanagement.controller;

import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentController {
    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/students";
    }

    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("student", new Student());
        return "students";
    }

    @PostMapping("/students")
    public String addStudent(@Valid @ModelAttribute Student student,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (studentRepository.existsByRollNumber(student.getRollNumber())) {
            result.rejectValue("rollNumber", "duplicate", "Roll number already exists");
        }
        if (studentRepository.existsByEmail(student.getEmail())) {
            result.rejectValue("email", "duplicate", "Email already exists");
        }
        if (result.hasErrors()) {
            model.addAttribute("students", studentRepository.findAll());
            return "students";
        }
        studentRepository.save(student);
        redirectAttributes.addFlashAttribute("success", "Student added successfully!");
        return "redirect:/students";
    }

    @GetMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student ID: " + id));
        model.addAttribute("student", student);
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("editMode", true);
        return "students";
    }

    @PostMapping("/students/update/{id}")
    public String updateStudent(@PathVariable Long id,
                                @Valid @ModelAttribute Student formStudent,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            formStudent.setId(id);
            model.addAttribute("students", studentRepository.findAll());
            model.addAttribute("editMode", true);
            return "students";
        }
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student ID: " + id));
        existing.setName(formStudent.getName());
        existing.setRollNumber(formStudent.getRollNumber());
        existing.setEmail(formStudent.getEmail());
        existing.setDepartment(formStudent.getDepartment());
        studentRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Student updated successfully!");
        return "redirect:/students";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        studentRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Student deleted successfully!");
        return "redirect:/students";
    }
}
