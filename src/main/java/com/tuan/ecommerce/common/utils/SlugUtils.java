package com.tuan.ecommerce.common.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String makeSlug(String input) {
        if (input == null) return "";
        
        // Chuyển về chữ thường
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        
        // Loại bỏ dấu tiếng Việt và ký tự đặc biệt
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        
        return slug.toLowerCase(Locale.ENGLISH)
                .replaceAll("-{2,}", "-") // Thay thế nhiều dấu gạch ngang liên tiếp
                .replaceAll("^-|-$", ""); // Loại bỏ dấu gạch ngang ở đầu và cuối
    }
}
