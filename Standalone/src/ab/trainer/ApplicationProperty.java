package ab.trainer;

public enum ApplicationProperty {
    lastOpenedFile("last.opened.file"), 
    lastOpenedDirectory;
    
    private String value;

    private ApplicationProperty(String value) {
        this.value = value;
    }

    private ApplicationProperty() {
        this.value = this.name();
    }

    public String getValue() {
        return value;
    }
}
