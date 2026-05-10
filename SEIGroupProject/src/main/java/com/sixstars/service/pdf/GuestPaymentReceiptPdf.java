package com.sixstars.service.pdf;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.GuestPaymentRecord;

/**
 * Generates a formal guest folio receipt PDF (hotel-style layout) and opens it in the default browser via {@code file:} URI.
 */
public final class GuestPaymentReceiptPdf {

    private static final float PAGE_W = PDRectangle.LETTER.getWidth();
    private static final float PAGE_H = PDRectangle.LETTER.getHeight();
    private static final float MARGIN = 54;
    private static final DateTimeFormatter RECEIPT_TS =
            DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a", Locale.US);

    private GuestPaymentReceiptPdf() {
    }

    /**
     * Writes a receipt PDF and attempts to open it in the system browser; falls back to desktop “open” or a path dialog.
     */
    public static void openReceiptInBrowser(java.awt.Component parent, GuestPaymentRecord record) {
        if (record == null) {
            return;
        }
        try {
            Path pdf = writeReceiptPdf(record, resolveGuestDisplayName(record));
            URI uri = pdf.toUri();
            boolean opened = false;
            if (!GraphicsEnvironment.isHeadless() && Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                    opened = true;
                } else if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(pdf.toFile());
                    opened = true;
                }
            }
            if (!opened) {
                JOptionPane.showMessageDialog(parent,
                        "Receipt saved to:\n" + pdf.toAbsolutePath() + "\n\nOpen this file manually in your browser or PDF viewer.",
                        "Receipt saved",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            JOptionPane.showMessageDialog(parent,
                    "Could not create or open the receipt PDF:\n" + msg,
                    "Receipt error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String resolveGuestDisplayName(GuestPaymentRecord record) {
        Account current = AccountController.currentAccount;
        if (current != null && current.getEmail() != null
                && current.getEmail().equalsIgnoreCase(record.getGuestEmail())) {
            String fn = current.getFirstName() == null ? "" : current.getFirstName().trim();
            String ln = current.getLastName() == null ? "" : current.getLastName().trim();
            String full = (fn + " " + ln).trim();
            if (!full.isBlank()) {
                return full;
            }
        }
        return record.getGuestEmail();
    }

    public static Path writeReceiptPdf(GuestPaymentRecord record, String guestDisplayName) throws IOException {
        Path out = Files.createTempFile("sixstars-receipt-" + record.getId() + "-", ".pdf");

        PDType1Font helv = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font helvBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 72;

                // Gold accent rule (brand)
                cs.setStrokingColor(0.59f, 0.475f, 0.26f);
                cs.setLineWidth(3f);
                cs.moveTo(MARGIN, PAGE_H - y);
                cs.lineTo(PAGE_W - MARGIN, PAGE_H - y);
                cs.stroke();
                y += 28;

                drawCenter(cs, helvBold, 20, PAGE_H - y, "S I X   S T A R S   H O T E L");
                y += 26;
                drawCenter(cs, helv, 10, PAGE_H - y, "A Luxury Collection Property Waco, Texas");
                y += 36;

                drawLeft(cs, helvBold, 14, MARGIN, PAGE_H - y, "OFFICIAL GUEST FOLIO RECEIPT");
                y += 22;
                drawLeft(cs, helv, 9, MARGIN, PAGE_H - y,
                        "This document is your receipt of payment for charges posted to your in-house guest ledger.");
                y += 36;

                // Receipt meta box
                float boxTop = PAGE_H - y;
                cs.setStrokingColor(0.75f, 0.72f, 0.68f);
                cs.setLineWidth(0.5f);
                float boxH = 118;
                cs.addRect(MARGIN, boxTop - boxH, PAGE_W - 2 * MARGIN, boxH);
                cs.stroke();
                float innerY = y + 14;
                drawLeft(cs, helvBold, 10, MARGIN + 12, PAGE_H - innerY, "Receipt / confirmation number");
                drawLeft(cs, helv, 11, MARGIN + 160, PAGE_H - innerY, "SSR-" + String.format(Locale.US, "%08d", record.getId()));
                innerY += 18;
                drawLeft(cs, helvBold, 10, MARGIN + 12, PAGE_H - innerY, "Transaction date & time");
                drawLeft(cs, helv, 11, MARGIN + 160, PAGE_H - innerY, record.getCreatedAt().format(RECEIPT_TS));
                innerY += 18;
                drawLeft(cs, helvBold, 10, MARGIN + 12, PAGE_H - innerY, "Guest folio name");
                drawLeft(cs, helv, 11, MARGIN + 160, PAGE_H - innerY, sanitizeOneLine(guestDisplayName, 72));
                innerY += 18;
                drawLeft(cs, helvBold, 10, MARGIN + 12, PAGE_H - innerY, "Guest email on file");
                drawLeft(cs, helv, 9, MARGIN + 160, PAGE_H - innerY, sanitizeOneLine(record.getGuestEmail(), 72));
                innerY += 18;
                drawLeft(cs, helvBold, 10, MARGIN + 12, PAGE_H - innerY, "Payment method");
                drawLeft(cs, helv, 9, MARGIN + 160, PAGE_H - innerY, sanitizeOneLine(record.getMethodSummary(), 72));
                y = innerY + 28;

                // Line items header (hotel folio style)
                drawLeft(cs, helvBold, 11, MARGIN, PAGE_H - y, "DESCRIPTION");
                drawRight(cs, helvBold, 11, PAGE_W - MARGIN, PAGE_H - y, "AMOUNT USD");
                y += 16;
                cs.setStrokingColor(0.35f, 0.35f, 0.35f);
                cs.setLineWidth(0.75f);
                cs.moveTo(MARGIN, PAGE_H - y);
                cs.lineTo(PAGE_W - MARGIN, PAGE_H - y);
                cs.stroke();
                y += 14;

                String lineDesc = "Payment received " + record.getKind().getDisplay();
                drawLeft(cs, helv, 10, MARGIN, PAGE_H - y, wrapDescription(lineDesc, 70));
                drawRight(cs, helvBold, 12, PAGE_W - MARGIN, PAGE_H - y,
                        String.format(Locale.US, "%s%,.2f", "$", record.getAmount()));
                y += 36;

                cs.setLineWidth(0.5f);
                cs.moveTo(MARGIN, PAGE_H - y);
                cs.lineTo(PAGE_W - MARGIN, PAGE_H - y);
                cs.stroke();
                y += 18;

                drawRight(cs, helvBold, 13, PAGE_W - MARGIN, PAGE_H - y,
                        "TOTAL PAID  " + String.format(Locale.US, "%s%,.2f", "$", record.getAmount()));
                y += 22;
                cs.setStrokingColor(0.55f, 0.52f, 0.48f);
                cs.setLineWidth(0.4f);
                cs.moveTo(MARGIN, PAGE_H - y);
                cs.lineTo(PAGE_W - MARGIN, PAGE_H - y);
                cs.stroke();
                y += 32;

                drawCenter(cs, helv, 8, PAGE_H - y,
                        "Six Stars Hotel LLC Federal EIN 87-6543210 demonstration property not a legal tax document");
                y += 22;

                y += drawLeftBlock(cs, helv, 9, MARGIN, PAGE_H - y,
                        "Amount shown in United States dollars USD. For international guests, card issuer exchange "
                                + "rates and international service fees may apply per your card agreement.");
                y += drawLeftBlock(cs, helv, 9, MARGIN, PAGE_H - y,
                        "This receipt reflects payment recorded in the Six Stars property management system. "
                                + "It is not a tax invoice unless separately issued by the hotel finance office.");
                y += 20;

                drawLeft(cs, helvBold, 9, MARGIN, PAGE_H - y, "PROPERTY INFORMATION");
                y += 14;
                drawLeft(cs, helv, 9, MARGIN, PAGE_H - y,
                        "Six Stars Hotel 1000 University Parks Drive Waco, TX 76706 United States");
                y += 12;
                drawLeft(cs, helv, 9, MARGIN, PAGE_H - y,
                        "Front desk: +1 254 555-0142 Reservations: reservations@6stars.xyz");
                y += 28;

                drawLeft(cs, helv, 8, MARGIN, PAGE_H - y,
                        "Retain for your records. Questions about this charge should reference receipt number SSR-"
                                + String.format(Locale.US, "%08d", record.getId()) + ".");
                y += 56;

                drawCenter(cs, helv, 9, PAGE_H - y, "Thank you for choosing Six Stars Hotel.");
            }

            doc.save(out.toFile());
        }
        return out;
    }

    /** @return vertical space consumed (from top), for advancing {@code y}. */
    private static float drawLeftBlock(PDPageContentStream cs, PDType1Font font, float size, float x, float yTopBaseline,
            String text) throws IOException {
        float baseline = yTopBaseline;
        int lines = 0;
        for (String line : splitLines(text)) {
            cs.beginText();
            cs.setFont(font, size);
            cs.newLineAtOffset(x, baseline);
            cs.showText(sanitizePdfText(line));
            cs.endText();
            baseline -= size + 3;
            lines++;
        }
        return lines * (size + 3) + 6;
    }

    private static void drawLeft(PDPageContentStream cs, PDType1Font font, float size, float x, float yBaseline, String text)
            throws IOException {
        drawLeftBlock(cs, font, size, x, yBaseline, text);
    }

    private static void drawCenter(PDPageContentStream cs, PDType1Font font, float size, float yBaseline, String text)
            throws IOException {
        String t = sanitizePdfText(text);
        float w = font.getStringWidth(t) / 1000f * size;
        float x = (PAGE_W - w) / 2f;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, yBaseline);
        cs.showText(t);
        cs.endText();
    }

    private static void drawRight(PDPageContentStream cs, PDType1Font font, float size, float rightX, float yBaseline, String text)
            throws IOException {
        String t = sanitizePdfText(text);
        float w = font.getStringWidth(t) / 1000f * size;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(rightX - w, yBaseline);
        cs.showText(t);
        cs.endText();
    }

    /** PDFWinAnsiEncoding: avoid chars outside WinAnsi for Type1 fonts. */
    private static String sanitizePdfText(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 32 && c <= 126 && c != '\\' && c != '(' && c != ')') {
                b.append(c);
            } else if (c == '\n') {
                b.append(' ');
            } else if (Character.isWhitespace(c)) {
                b.append(' ');
            } else {
                b.append('?');
            }
        }
        return b.toString().trim();
    }

    private static String sanitizeOneLine(String s, int maxLen) {
        String t = sanitizePdfText(s == null ? "" : s.replace('\n', ' '));
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, maxLen - 1) + "…";
    }

    private static List<String> splitLines(String text) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isBlank()) {
            out.add("");
            return out;
        }
        for (String raw : text.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            out.addAll(wrapToWidth(line, 88));
        }
        if (out.isEmpty()) {
            out.add("");
        }
        return out;
    }

    /** Word-wrap at spaces so lines fit a letter-width column. */
    private static List<String> wrapToWidth(String line, int maxChars) {
        List<String> rows = new ArrayList<>();
        String remaining = line;
        while (!remaining.isEmpty()) {
            if (remaining.length() <= maxChars) {
                rows.add(remaining);
                break;
            }
            int cut = maxChars;
            int space = remaining.lastIndexOf(' ', cut);
            if (space > maxChars / 2) {
                cut = space;
            }
            rows.add(remaining.substring(0, cut).trim());
            remaining = remaining.substring(cut).trim();
        }
        return rows;
    }

    private static String wrapDescription(String desc, int maxChars) {
        String clean = sanitizePdfText(desc);
        if (clean.length() <= maxChars) {
            return clean;
        }
        return clean.substring(0, maxChars - 1) + "…";
    }
}
