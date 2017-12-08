package cn.skyeye.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Excels {

    private Excels(){}

    public static List<SheetEntry> readExcel(String file) throws IOException{
        if(file != null){
            return readExcel(new File(file));
        }
        return Lists.newArrayList();
    }

    public static List<SheetEntry> readExcel(File file) throws IOException {
        if(file != null) {
            InputStream inputStream = new FileInputStream(file);
            try {
                if (file.getName().endsWith("xlsx")) {
                    //处理ecxel2007
                    return readExcel2007(inputStream);
                } else {
                    //处理ecxel2003
                    return readExcel2003(inputStream);
                }
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
        return  Lists.newArrayList();
    }

    public static List<SheetEntry> readExcel2007(InputStream inputStream) throws IOException {
       return readExcel(new XSSFWorkbook(inputStream));
    }

    public static List<SheetEntry> readExcel2003(InputStream inputStream) throws IOException {
       return readExcel(new HSSFWorkbook(inputStream));
    }

    private static List<SheetEntry> readExcel(Workbook wb) {
        //获取有多少个sheet页
        List<SheetEntry> sheetEntries = Lists.newArrayList();
        Map<Integer,  List<CellEntry>> sheetMap;
        Sheet sheet;
        for(int i = 0; i < wb.getNumberOfSheets(); i++){
            sheet = wb.getSheetAt(i);
            sheetMap = readSheet(sheet);
            sheetEntries.add(new SheetEntry(i, sheet.getSheetName(), sheetMap));
        }
        return sheetEntries;
    }

    private static Map<Integer, List<CellEntry>> readSheet(Sheet sheet){
        Map<Integer,  List<CellEntry>> sheetMap = Maps.newHashMap();
        List<CellEntry> cellEntries;
        Row row;
        for(int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++){
            row = sheet.getRow(i);
            if(row == null)continue;
            cellEntries = readRow(row);
            sheetMap.put(i, cellEntries);
        }
        return sheetMap;
    }

    private static List<CellEntry> readRow(Row row){
        List<CellEntry> cellEntries = Lists.newArrayList();
        Object val;
        Cell cell;
        for(int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++){
            cell = row.getCell(i);
            if(cell == null)continue;
            val = readCell(cell);
            cellEntries.add(new CellEntry(i, val, cell.getCellTypeEnum()));
        }
        return cellEntries;
    }

    private static Object readCell(Cell cell){
        CellType type = cell.getCellTypeEnum();
        switch (type) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                    return cell.getDateCellValue();
                return cell.getNumericCellValue();
            case STRING:
                return cell.getRichStringCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                return cell.getErrorCellValue();
                //ErrorEval.getText(getErrorCellValue());
            default:
                return cell.getStringCellValue();
        }
    }

    public static void write(List<SheetEntry> sheetEntries, String path) throws IOException {
        if(sheetEntries != null && path != null){
            if (path.endsWith("xlsx")) {
                writeExcel2007(sheetEntries, path);
            } else {
                writeExcel2003(sheetEntries, path);
            }
        }
    }

    public static void writeExcel2007(List<SheetEntry> sheetEntries, String path) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        write(wb, sheetEntries, path);
    }

    public static void writeExcel2003(List<SheetEntry> sheetEntries, String path) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        write(wb, sheetEntries, path);

    }

    private static void write(Workbook wb, List<SheetEntry> sheetEntries, String path) throws IOException {
        //按sheet的下标排序
        Collections.sort(sheetEntries);
        sheetEntries.forEach(sheetEntry -> {
            Sheet sheet = wb.createSheet(sheetEntry.sheetName);
            sheetEntry.getRowData().forEach((rowIndex, rowEntry) ->{
                Row row = sheet.createRow(rowIndex);
                rowEntry.forEach(cellEntry ->{
                    Cell cell = row.createCell(cellEntry.index, cellEntry.getType());
                    switch (cellEntry.type){
                        case NUMERIC:
                            if (cellEntry.isDateFormatted())
                                cell.setCellValue(cellEntry.getDate());
                            cell.setCellValue(cellEntry.getDouble());
                            break;
                        case STRING:
                            Object value = cellEntry.value;
                            if(value instanceof RichTextString){
                                cell.setCellValue((RichTextString)value);
                            }else {
                                cell.setCellValue(cellEntry.getString());
                            }
                            break;
                        case FORMULA:
                            cell.setCellValue(cellEntry.getString());
                            break;
                        case BLANK:
                            cell.setCellValue("");
                            break;
                        case BOOLEAN:
                            cell.setCellValue(cellEntry.getBoolean());
                            break;
                        case ERROR:
                            cell.setCellValue(cellEntry.getByte());
                            break;
                        default:
                            cell.setCellValue(cellEntry.getString());
                            break;
                    }
                });
            });
        });

        write(wb, path);
    }

    private static void write(Workbook wb, String path) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        byte[] content = os.toByteArray();
        FileUtils.writeByteArrayToFile(new File(path), content);
    }

    public static class SheetEntry implements Comparable<SheetEntry>{
        private int sheetIndex;
        private String sheetName;
        private Map<Integer,  List<CellEntry>> rowData;

        public SheetEntry() { }

        public SheetEntry(int sheetIndex,
                          String sheetName,
                          Map<Integer,  List<CellEntry>> rowData) {
            this.sheetIndex = sheetIndex;
            this.sheetName = sheetName;
            this.rowData = rowData;
        }

        public void setSheetIndex(int sheetIndex) {
            this.sheetIndex = sheetIndex;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }

        public void setRowData(Map<Integer, List<CellEntry>> rowData) {
            this.rowData = rowData;
        }

        public int getSheetIndex() {
            return sheetIndex;
        }

        public String getSheetName() {
            return sheetName;
        }

        public Map<Integer,  List<CellEntry>> getRowData() {
            return rowData;
        }

        @Override
        public int compareTo(SheetEntry o) {
            return Integer.compare(sheetIndex, o.sheetIndex);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SheetEntry{");
            sb.append("sheetIndex=").append(sheetIndex);
            sb.append(", sheetName='").append(sheetName).append('\'');
            sb.append(", rowData=").append(rowData);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class CellEntry{
        private int index;
        private Object value;
        private CellType type;
        private boolean DateFormatted;

        public CellEntry() {}

        public CellEntry(int index, Object value, CellType type) {
            this.index = index;
            this.value = value;
            this.type = type;
        }

        public boolean isDateFormatted() {
            return DateFormatted;
        }

        public void setDateFormatted(boolean dateFormatted) {
            DateFormatted = dateFormatted;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Object getValue() {
            return value;
        }

        public Date getDate(){
            return (Date)value;
        }

        public double getDouble(){
            return (double)value;
        }
        public String getString(){
            return (String)value;
        }

        public boolean getBoolean(){
            return (boolean) value;
        }

        public byte getByte(){
            return (byte) value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public CellType getType() {
            return type;
        }

        public void setType(CellType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("CellEntry{");
            sb.append("index=").append(index);
            sb.append(", value=").append(value);
            sb.append(", type=").append(type);
            sb.append(", DateFormatted=").append(DateFormatted);
            sb.append('}');
            return sb.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        List<SheetEntry> sheetEntries = readExcel("D:\\logs\\demo\\receiver.xlsx");
        sheetEntries.forEach(entry->{
            entry.getRowData().forEach((rowIndex, rowEntry) -> {
                rowEntry.forEach(cellEntry ->{
                    System.out.println(String.format("第%s页第%s行第%s列：%s",
                            entry.sheetIndex, rowIndex, cellEntry.index, cellEntry.value));
                });
            });
        });
    }
}