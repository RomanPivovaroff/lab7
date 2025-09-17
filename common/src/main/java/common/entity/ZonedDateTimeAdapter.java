package common.entity;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Класс-адаптер для JAXB. */
public class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public ZonedDateTime unmarshal(String value) throws Exception {
        return value != null ? ZonedDateTime.parse(value, FORMATTER) : null;
    }

    @Override
    public String marshal(ZonedDateTime value) throws Exception {
        return value != null ? value.format(FORMATTER) : null;
    }
}
