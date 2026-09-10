package com.accreditation.nba.evidence.report;

import com.accreditation.nba.evidence.exception.BadRequestException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * Renders tabular report data to XLSX (Apache POI), PDF (PDFBox) or CSV. Kept generic
 * (title + headers + rows) so any evidence report can reuse it.
 */
@Slf4j
@Component
public class ReportExporter {

    public static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String PDF_CONTENT_TYPE = "application/pdf";
    public static final String CSV_CONTENT_TYPE = "text/csv";

    public ReportFile export(String format, String baseFileName, String title,
                             List<String> headers, List<List<String>> rows) {
        String fmt = format == null ? "xlsx" : format.toLowerCase();
        return switch (fmt) {
            case "xlsx" -> new ReportFile(baseFileName + ".xlsx", XLSX_CONTENT_TYPE, excel(title, headers, rows));
            case "csv" -> new ReportFile(baseFileName + ".csv", CSV_CONTENT_TYPE, csv(headers, rows));
            case "pdf" -> new ReportFile(baseFileName + ".pdf", PDF_CONTENT_TYPE, pdf(title, headers, rows));
            default -> throw new BadRequestException("Unsupported report format: " + format + " (use xlsx|pdf|csv)");
        };
    }

    private byte[] excel(String title, List<String> headers, List<List<String>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Report");

            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);

            int rowIdx = 0;
            Row headerRow = sheet.createRow(rowIdx++);
            for (int c = 0; c < headers.size(); c++) {
                Cell cell = headerRow.createCell(c);
                cell.setCellValue(headers.get(c));
                cell.setCellStyle(headerStyle);
            }
            for (List<String> row : rows) {
                Row dataRow = sheet.createRow(rowIdx++);
                for (int c = 0; c < row.size(); c++) {
                    dataRow.createCell(c).setCellValue(row.get(c) == null ? "" : row.get(c));
                }
            }
            for (int c = 0; c < headers.size(); c++) {
                sheet.autoSizeColumn(c);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BadRequestException("Failed to generate Excel report: " + e.getMessage());
        }
    }

    private byte[] csv(List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        appendCsvRow(sb, headers);
        for (List<String> row : rows) {
            appendCsvRow(sb, row);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendCsvRow(StringBuilder sb, List<String> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeCsv(cells.get(i)));
        }
        sb.append("\r\n");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private byte[] pdf(String title, List<String> headers, List<List<String>> rows) {
        List<List<String>> allRows = new ArrayList<>();
        allRows.add(headers);
        allRows.addAll(rows);

        // Landscape A4 to fit wide tables.
        PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
        PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        float margin = 30f;
        float rowHeight = 16f;
        float fontSize = 8f;

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();
            int colCount = Math.max(headers.size(), 1);
            float colWidth = (pageWidth - 2 * margin) / colCount;

            PDPage page = new PDPage(pageSize);
            document.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(document, page);

            drawText(cs, bold, 14f, margin, pageHeight - margin, safe(title));
            float y = pageHeight - margin - 24f;

            for (int r = 0; r < allRows.size(); r++) {
                if (y < margin + rowHeight) {
                    cs.close();
                    page = new PDPage(pageSize);
                    document.addPage(page);
                    cs = new PDPageContentStream(document, page);
                    y = pageHeight - margin;
                }
                List<String> row = allRows.get(r);
                boolean isHeader = r == 0;
                PDFont rowFont = isHeader ? bold : font;
                for (int c = 0; c < colCount; c++) {
                    String text = c < row.size() ? safe(row.get(c)) : "";
                    text = truncate(text, rowFont, fontSize, colWidth - 4f);
                    drawText(cs, rowFont, fontSize, margin + c * colWidth, y, text);
                }
                y -= rowHeight;
            }
            cs.close();

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BadRequestException("Failed to generate PDF report: " + e.getMessage());
        }
    }

    private void drawText(PDPageContentStream cs, PDFont font, float size, float x, float y, String text)
            throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private String truncate(String text, PDFont font, float fontSize, float maxWidth) {
        try {
            if (stringWidth(font, fontSize, text) <= maxWidth) {
                return text;
            }
            String ellipsis = "..";
            StringBuilder sb = new StringBuilder(text);
            while (sb.length() > 1 && stringWidth(font, fontSize, sb + ellipsis) > maxWidth) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb + ellipsis;
        } catch (IOException e) {
            return text;
        }
    }

    private float stringWidth(PDFont font, float fontSize, String text) throws IOException {
        return font.getStringWidth(text) / 1000f * fontSize;
    }

    /** Keep only WinAnsi-safe printable ASCII so PDFBox never fails on exotic characters. */
    private String safe(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char ch : value.toCharArray()) {
            sb.append(ch >= 32 && ch < 127 ? ch : '?');
        }
        return sb.toString();
    }
}
