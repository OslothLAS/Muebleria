package com.example.Ecommerce_Muebleria.config;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * 🛡️ Limpia el HTML que se muestra con th:utext (ej: notas del vendedor hechas con Summernote).
 * Conserva el formato (negrita, listas, links, imágenes) y elimina <script>, onclick=, javascript:, etc.
 */
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.relaxed();

    private HtmlSanitizer() {
    }

    public static String clean(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, SAFELIST);
    }
}
