package cn.skyeye.common.geo;

public class GeoHashLatLon {
    private String geoHashCode;
    private String centreLat;
    private String centrelon;
    private String leftDownLat;
    private String leftDownLon;
    private String leftUpLat;
    private String leftUpLon;
    private String rightDownLat;
    private String rightDownLon;
    private String rightUpLat;
    private String rightUpLon;
    private String rowColumn;

    public GeoHashLatLon(String geoHashCode, String centreLat, String centrelon, String leftDownLat, String leftDownLon, String leftUpLat,
                         String leftUpLon, String rightDownLat, String rightDownLon, String rightUpLat, String rightUpLon, String rowColumn){
        this.geoHashCode = geoHashCode;
        this.centreLat = centreLat;
        this.centrelon = centrelon;
        this.leftDownLat = leftDownLat;
        this.leftDownLon = leftDownLon;
        this.leftUpLat = leftUpLat;
        this.leftUpLon = leftUpLon;
        this.rightDownLat = rightDownLat;
        this.rightDownLon = rightDownLon;
        this.rightUpLat = rightUpLat;
        this.rightUpLon = rightUpLon;
        this.rowColumn = rowColumn;
    }


    public void setGeoHashCode(String geoHashCode) {
        this.geoHashCode = geoHashCode;
    }
    public String getGeoHashCode() {
        return geoHashCode;
    }
    public void setCentreLat(String centreLat) {
        this.centreLat = centreLat;
    }
    public String getCentreLat() {
        return centreLat;
    }
    public void setCentrelon(String centrelon) {
        this.centrelon = centrelon;
    }
    public String getCentrelon() {
        return centrelon;
    }
    public void setLeftDownLat(String leftDownLat) {
        this.leftDownLat = leftDownLat;
    }
    public String getLeftDownLat() {
        return leftDownLat;
    }
    public void setLeftDownLon(String leftDownLon) {
        this.leftDownLon = leftDownLon;
    }
    public String getLeftDownLon() {
        return leftDownLon;
    }
    public void setLeftUpLat(String leftUpLat) {
        this.leftUpLat = leftUpLat;
    }
    public String getLeftUpLat() {
        return leftUpLat;
    }
    public void setLeftUpLon(String leftUpLon) {
        this.leftUpLon = leftUpLon;
    }
    public String getLeftUpLon() {
        return leftUpLon;
    }
    public void setRightDownLat(String rightDownLat) {
        this.rightDownLat = rightDownLat;
    }
    public String getRightDownLat() {
        return rightDownLat;
    }
    public void setRightDownLon(String rightDownLon) {
        this.rightDownLon = rightDownLon;
    }
    public String getRightDownLon() {
        return rightDownLon;
    }
    public void setRightUpLat(String rightUpLat) {
        this.rightUpLat = rightUpLat;
    }
    public String getRightUpLat() {
        return rightUpLat;
    }
    public void setRightUpLon(String rightUpLon) {
        this.rightUpLon = rightUpLon;
    }
    public String getRightUpLon() {
        return rightUpLon;
    }
    public void setRowColumn(String rowColumn) {
        this.rowColumn = rowColumn;
    }
    public String getRowColumn() {
        return rowColumn;
    }

}
