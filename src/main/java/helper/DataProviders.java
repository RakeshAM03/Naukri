package helper;

import org.testng.annotations.DataProvider;

/**
 * Central home for all TestNG @DataProvider methods.
 * Backed by ExcelReader so data-driven tests pull rows straight from
 * testdata/testdata.xlsx.
 */
public class DataProviders {

    @DataProvider(name = "loginData")
    public Object[][] getLoginData() {
        var excelPath = ConfigReader.get("excelFilePath");
        var sheetName = ConfigReader.get("excelSheetName");

        var excelReader = new ExcelReader(excelPath);
        var rows = excelReader.getSheetData(sheetName);

        return rows.stream()
                .map(row -> (Object[]) row)
                .toArray(Object[][]::new);
    }
}
