package com.example.prog4.service;

import com.example.prog4.config.CompanyConf;
import com.example.prog4.model.exception.InternalServerErrorException;
import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.example.prog4.model.Employee;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

@Component
public class PDFUtils {

    private String parseThymeleafTemplate(Employee employee, CompanyConf companyConf, String template) {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setTemplateMode(HTML);
        templateResolver.setOrder(1);

        TemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("employee", employee);
        context.setVariable("companyConf", companyConf);
        return templateEngine.process(template, context);
    }
    public byte[] generatePdf(Employee employee) {
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(parseThymeleafTemplate(employee, new CompanyConf(), "employee_sheet"));
            renderer.layout();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
