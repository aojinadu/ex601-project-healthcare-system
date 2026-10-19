package edu.bu.ex601.health.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/** A diagnosis on a visit note. Only the code's format is checked (ICD-10 style, e.g. J45.909). */
public class Diagnosis {
    private static final Pattern CODE_FORMAT = Pattern.compile("[A-Z][0-9]{2}(\\.[0-9A-Z]{1,4})?");

    private final String code;
    private final String description;

    public Diagnosis(String code, String description) {
        this.code = Objects.requireNonNull(code);
        this.description = Objects.requireNonNull(description);
    }

    public boolean hasValidCodeFormat() {
        return CODE_FORMAT.matcher(code).matches();
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
