package com.electricpro.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private final int navy = Color.rgb(4, 20, 39);
    private final int panel = Color.rgb(10, 43, 76);
    private final int panel2 = Color.rgb(13, 53, 91);
    private final int yellow = Color.rgb(255, 190, 35);
    private final int white = Color.rgb(245, 248, 255);
    private final int muted = Color.rgb(183, 204, 226);
    private LinearLayout root;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(navy);
        home();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), Color.rgb(28, 75, 113));
        return drawable;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(
                width < 0 ? width : dp(width),
                height < 0 ? height : dp(height)
        );
    }

    private void page(String title, String subtitle, boolean back) {
        root = column();
        root.setPadding(dp(18), dp(18), dp(18), dp(24));
        root.setBackgroundColor(navy);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(navy);
        scroll.addView(root);
        setContentView(scroll);

        if (back) {
            TextView top = text("‹    " + title, 20, white, true);
            top.setPadding(0, dp(4), 0, dp(16));
            top.setOnClickListener(v -> home());
            root.addView(top, lp(-1, -2));
        } else {
            TextView logo = text("⚡", 42, yellow, true);
            logo.setGravity(Gravity.CENTER);
            root.addView(logo, lp(-1, 54));

            TextView brand = text("ЭЛЕКТРИК ", 27, white, true);
            brand.setGravity(Gravity.CENTER);

            TextView pro = text("PRO", 27, yellow, true);
            pro.setGravity(Gravity.CENTER);

            LinearLayout name = new LinearLayout(this);
            name.setGravity(Gravity.CENTER);
            name.addView(brand);
            name.addView(pro);
            root.addView(name, lp(-1, 38));
        }

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView sub = text(subtitle, 14, muted, false);
            sub.setGravity(back ? Gravity.START : Gravity.CENTER);
            sub.setPadding(0, dp(4), 0, dp(18));
            root.addView(sub, lp(-1, -2));
        }
    }

    private void addSpace(int height) {
        View space = new View(this);
        root.addView(space, lp(1, height));
    }

    private void actionButton(String label, Runnable action) {
        TextView button = text(label, 16, navy, true);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(12), dp(13), dp(12), dp(13));
        button.setBackground(bg(yellow, 12));

        LinearLayout.LayoutParams params = lp(-1, -2);
        params.topMargin = dp(8);
        params.bottomMargin = dp(5);
        root.addView(button, params);
        button.setOnClickListener(v -> action.run());
    }

    private void addGridCardRow(
            String icon1, String title1, String description1, Runnable action1,
            String icon2, String title2, String description2, Runnable action2) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams leftParams =
                new LinearLayout.LayoutParams(0, dp(132), 1);
        leftParams.rightMargin = dp(6);

        LinearLayout.LayoutParams rightParams =
                new LinearLayout.LayoutParams(0, dp(132), 1);
        rightParams.leftMargin = dp(6);

        // Add each card directly to the row. This avoids adding a view
        // that is still attached to another parent, which can crash the app.
        row.addView(makeGridCard(icon1, title1, description1, action1), leftParams);
        row.addView(makeGridCard(icon2, title2, description2, action2), rightParams);

        LinearLayout.LayoutParams rowParams = lp(-1, -2);
        rowParams.bottomMargin = dp(10);
        root.addView(row, rowParams);
    }

    private View makeGridCard(String icon, String title, String description, Runnable action) {
        LinearLayout card = column();
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setBackground(bg(panel, 14));

        TextView iconView = text(icon, 28, yellow, true);
        iconView.setGravity(Gravity.CENTER);
        card.addView(iconView, lp(-1, 38));

        TextView titleView = text(title, 15, white, true);
        titleView.setGravity(Gravity.CENTER);
        card.addView(titleView, lp(-1, -2));

        TextView descriptionView = text(description, 11, muted, false);
        descriptionView.setGravity(Gravity.CENTER);
        descriptionView.setPadding(0, dp(4), 0, 0);
        card.addView(descriptionView, lp(-1, -2));

        card.setOnClickListener(v -> action.run());
        return card;
    }

    private void infoCard(String title, String body) {
        LinearLayout card = column();
        card.setPadding(dp(15), dp(14), dp(15), dp(14));
        card.setBackground(bg(panel, 13));

        card.addView(text(title, 16, white, true));

        TextView bodyView = text(body, 14, muted, false);
        bodyView.setPadding(0, dp(7), 0, 0);
        card.addView(bodyView);

        LinearLayout.LayoutParams params = lp(-1, -2);
        params.bottomMargin = dp(10);
        root.addView(card, params);
    }

    private EditText field(String label, String initialValue) {
        TextView labelView = text(label, 13, muted, false);
        LinearLayout.LayoutParams labelParams = lp(-1, -2);
        labelParams.topMargin = dp(10);
        root.addView(labelView, labelParams);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(initialValue);
        input.setTextColor(white);
        input.setHintTextColor(muted);
        input.setTextSize(16);
        input.setPadding(dp(12), dp(8), dp(12), dp(8));
        input.setBackground(bg(panel2, 9));
        input.setInputType(8194);

        root.addView(input, lp(-1, 48));
        return input;
    }

    private double value(EditText input) {
        try {
            return Double.parseDouble(
                    input.getText().toString().trim().replace(',', '.')
            );
        } catch (Exception exception) {
            return Double.NaN;
        }
    }

    private boolean valid(double... values) {
        for (double value : values) {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return false;
            }
        }
        return true;
    }

    private void resultCard(String title, String result, String note) {
        LinearLayout card = column();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(bg(panel, 14));
        card.addView(text(title, 16, white, true));

        TextView resultView = text(result, 27, yellow, true);
        resultView.setPadding(0, dp(7), 0, dp(5));
        card.addView(resultView);

        if (note != null && !note.isEmpty()) {
            card.addView(text(note, 13, muted, false));
        }

        LinearLayout.LayoutParams params = lp(-1, -2);
        params.topMargin = dp(14);
        root.addView(card, params);
    }

    private void home() {
        page("", "Ваш помощник в электромонтажных работах", false);

        TextView headline = text("Знания. Расчёты.\nБезопасность.", 24, white, true);
        headline.setGravity(Gravity.CENTER);
        headline.setPadding(0, dp(12), 0, dp(20));
        root.addView(headline, lp(-1, -2));

        addGridCardRow(
                "▦", "Калькулятор\nпроводов", "Ток и сечение", () -> calc(),
                "☼", "Освещение", "Световой поток", () -> light()
        );

        addGridCardRow(
                "⬟", "Безопасность", "Правила и защита", () -> safety(),
                "▤", "Справочник", "Формулы и таблицы", () -> reference()
        );

        actionButton("Начать", () -> calc());
        addSpace(8);

        TextView footer = text("Работай безопасно! Работай с умом!", 13, yellow, true);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, lp(-1, -2));
    }

    private void calc() {
        page("Калькулятор проводов",
                "Ориентировочный расчёт электрической нагрузки", true);

        infoCard("Расчёт по току",
                "Для однофазной нагрузки: I ≈ P / U. Для более точного расчёта учитывайте коэффициент мощности и тип нагрузки.");

        EditText power = field("Мощность нагрузки (Вт)", "3500");
        EditText voltage = field("Напряжение (В)", "220");
        EditText cos = field("Коэффициент мощности cos φ (1 для простого расчёта)", "1");

        actionButton("▦  Рассчитать ток", () -> {
            double p = value(power);
            double u = value(voltage);
            double c = value(cos);

            if (!valid(p, u, c) || p <= 0 || u <= 0 || c <= 0 || c > 1) {
                message("Введите корректные числа. Мощность и напряжение должны быть больше нуля, cos φ — от 0 до 1.");
                return;
            }

            double current = p / (u * c);
            resultCard("Расчётный ток",
                    String.format(Locale.getDefault(), "%.2f А", current),
                    "Это оценка нагрузки, а не готовый подбор кабеля или автомата.");
        });

        addSpace(12);
        infoCard("Падение напряжения",
                "Падение зависит от материала, сечения, длины линии, тока и схемы сети. Для выбора кабеля используйте действующие нормы и таблицы производителя.");

        EditText amps = field("Ток нагрузки (А)", "16");
        EditText length = field("Длина линии в одну сторону (м)", "20");
        EditText section = field("Сечение медного проводника (мм²)", "2.5");

        actionButton("Рассчитать падение напряжения", () -> {
            double current = value(amps);
            double lineLength = value(length);
            double area = value(section);

            if (!valid(current, lineLength, area)
                    || current < 0 || lineLength <= 0 || area <= 0) {
                message("Введите корректные значения: ток не может быть отрицательным, длина и сечение должны быть больше нуля.");
                return;
            }

            double drop = 2 * 0.0175 * lineLength * current / area;
            double percent = drop / 220 * 100;

            resultCard("Оценочное падение напряжения",
                    String.format(Locale.getDefault(), "%.2f В (%.1f%%)", drop, percent),
                    "Упрощённая оценка для медной однофазной линии при 220 В; не учитывает нагрев и реактивное сопротивление.");
        });

        infoCard("Важно",
                "Нельзя выбирать сечение только по этому калькулятору. Учитываются способ прокладки, материал, температура, группировка кабелей, длина, защита и местные нормы.");
    }

    private void light() {
        page("Освещение", "Быстрый ориентировочный расчёт", true);

        EditText area = field("Площадь помещения (м²)", "20");
        EditText lux = field("Требуемая освещённость (лк)", "300");
        EditText watts = field("Мощность одного светильника (Вт)", "12");
        EditText lumens = field("Световой поток одного светильника (лм)", "1200");

        actionButton("☼  Рассчитать освещение", () -> {
            double roomArea = value(area);
            double illumination = value(lux);
            double power = value(watts);
            double flux = value(lumens);

            if (!valid(roomArea, illumination, power, flux)
                    || roomArea <= 0 || illumination <= 0 || power <= 0 || flux <= 0) {
                message("Все значения должны быть больше нуля.");
                return;
            }

            double needed = roomArea * illumination;
            int count = (int) Math.ceil(needed / flux);

            resultCard("Ориентировочно светильников",
                    String.valueOf(count),
                    String.format(Locale.getDefault(),
                            "Суммарный поток: %.0f лм\nОриентировочная мощность: %.0f Вт",
                            count * flux, count * power));
        });

        infoCard("Примечание",
                "Расчёт не учитывает коэффициент использования, высоту монтажа, загрязнение и отражение поверхностей. Для рабочего проекта требуется светотехнический расчёт.");
    }

    private void safety() {
        page("Безопасность", "Основные правила перед началом работ", true);

        infoCard("⚠  Отключите питание",
                "Отключите нужную цепь, исключите случайное повторное включение и обозначьте место работ.");
        infoCard("✓  Проверьте отсутствие напряжения",
                "Используйте исправный двухполюсный указатель и проверьте его работу до и после проверки цепи.");
        infoCard("⬟  Используйте СИЗ",
                "Подбирайте защитные очки, перчатки, инструмент и одежду под конкретную работу и оценённый риск.");
        infoCard("＋  Первая помощь",
                "При поражении током сначала безопасно отключите питание. Не касайтесь пострадавшего, пока источник опасности не устранён. Вызовите экстренную помощь.");
        infoCard("⛨  Защитные устройства",
                "Автоматы, УЗО, заземление и сечения проводников выбирают по проекту, условиям установки и действующим нормам.");
        infoCard("Важно",
                "Не выполняйте работы под напряжением без специальной квалификации, допуска и соответствующих процедур.");
    }

    private void reference() {
        page("Справочник", "Базовые формулы и справочные значения", true);

        infoCard("Закон Ома",
                "I = U / R\nТок (А) равен напряжению (В), делённому на сопротивление (Ом).");
        infoCard("Электрическая мощность",
                "P = U × I\nДля переменного тока и реальной нагрузки учитывайте cos φ: P ≈ U × I × cos φ.");
        infoCard("Сопротивление",
                "R = U / I\nСопротивление измеряется в омах (Ω).");
        infoCard("Падение напряжения",
                "Для простой однофазной медной линии ориентировочно: ΔU ≈ 2 × 0,0175 × L × I / S, где L — длина в одну сторону (м), I — ток (А), S — сечение (мм²).");
        infoCard("Сечение кабеля — не выбирайте только по току",
                "Допустимый ток зависит от способа прокладки, изоляции, температуры, количества нагруженных жил, группировки, материала и защитного аппарата. Используйте действующие таблицы ПУЭ/местных норм и данные производителя.");
        infoCard("Цвета проводников",
                "Защитный PE обычно жёлто-зелёный; нейтральный N — голубой. Цвета и назначение необходимо проверять по схеме и нормам конкретной установки, не полагаться только на цвет.");
    }

    private void message(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Понятно", null)
                .show();
    }
}
