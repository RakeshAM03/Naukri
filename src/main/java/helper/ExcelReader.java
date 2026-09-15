package helper;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic Apache POI based reader for .xlsx test data files.
 *
 * Expected layout for the "login" sheet:
 *   Row 0 (header): email | password
 *   Row 1..n       : actual data rows
 *
 * Values may reference environment/system properties using the syntax
 * ${propertyName} so real credentials never need to be stored in the
 * checked-in spreadsheet, e.g. a cell containing "${naukri.email}"
 * will be resolved via ConfigReader/System property at read time.
 */
public class ExcelReader {

    private final String filePath;

    public ExcelReader(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads every data row (excluding the header row) from the given sheet
     * and returns it as a List of String[] rows, suitable for handing
     * straight to a TestNG @DataProvider.
     */
    public List<String[]> getSheetData(String sheetName) {
        List<String[]> rows = new ArrayList<>();

        try (var fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            var sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '%s' not found in %s".formatted(sheetName, filePath));
            }

            var lastRow = sheet.getLastRowNum();
            var columnCount = sheet.getRow(0).getLastCellNum();

            for (int r = 1; r <= lastRow; r++) {
                var row = sheet.getRow(r);
                if (row == null) continue;

                var rowData = new String[columnCount];
                var rowHasData = false;

                for (int c = 0; c < columnCount; c++) {
                    var cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    var value = getCellValueAsString(cell);
                    rowData[c] = resolvePlaceholder(value);
                    if (value != null && !value.trim().isEmpty()) {
                        rowHasData = true;
                    }
                }

                if (rowHasData) {
                    rows.add(rowData);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }

        return rows;
    }

    /**
     * Resolves "${key}" style placeholders against system properties /
     * environment variables so secrets don't have to live in the sheet.
     * Non-placeholder values are returned unchanged.
     */
    private String resolvePlaceholder(String value) {
        if (value == null) return null;
        var trimmed = value.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {
            var key = trimmed.substring(2, trimmed.length() - 1);
            var resolved = System.getProperty(key);
            if (resolved == null || resolved.isEmpty()) {
                resolved = System.getenv(key);
            }
            if (resolved == null || resolved.isEmpty()) {
                throw new RuntimeException(
                        "Excel cell references '%s' but no matching system property or environment variable was found. Pass it via -D%s=value or set the environment variable %s."
                                .formatted(trimmed, key, key));
            }
            return resolved;
        }
        return value;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                var num = cell.getNumericCellValue();
                yield num == Math.floor(num) ? String.valueOf((long) num) : String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK, _NONE, ERROR -> "";
        };
    }
}
