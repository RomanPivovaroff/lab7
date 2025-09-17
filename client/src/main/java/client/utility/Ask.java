package client.utility;

import common.entity.*;
import common.utility.Console;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

/** Клаас чтение элемента при добавлении его в коллекцию. */
public class Ask {
    public static class AskBreak extends Exception {}

    public static Worker AskWorker(Console console, int id) throws AskBreak {
        console.println("* Создание нового Worker:");
        try {
            String name;
            while (true) {
                console.print("name: ");
                name = console.readln().trim();
                if (name.equals("exit")) throw new AskBreak();
                if (!name.isEmpty()) break;
            }
            Coordinates coordinates = askCoordinates(console);

            Integer salary = 0;
            while (true) {
                console.print("salary(Integer salary>0):");
                String line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (line.isEmpty()) break;
                try {
                    salary = Integer.parseInt(line);
                    if (salary > 0) break;
                } catch (NumberFormatException e) {
                }
            }

            ZonedDateTime startDate;
            while (true) {
                console.print(
                        "date-time (Example: "
                                + ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
                                + " or 2023-03-11 or 2023-03-11T15:30+03:00): ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                try {
                    // полный формат с временной зоной
                    startDate = ZonedDateTime.parse(line, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    break;
                } catch (DateTimeParseException e) {
                }
                try {
                    // формат без временной зоны
                    LocalDateTime ldt =
                            LocalDateTime.parse(line, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    startDate = ldt.atZone(ZoneId.systemDefault());
                    break;
                } catch (DateTimeParseException e) {
                }
                try {
                    // формат только с датой
                    LocalDate ld = LocalDate.parse(line);
                    startDate = ld.atStartOfDay(ZoneId.systemDefault());
                    break;
                } catch (DateTimeParseException e) {
                }
            }
            Position position;
            while (true) {
                console.print(
                        "Position (DIRECTOR, LABORER, HUMAN_RESOURCES, HEAD_OF_DEPARTMENT, COOK):"
                                + " ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.isEmpty()) {
                    try {
                        position = Position.valueOf(line);
                        break;
                    } catch (NullPointerException | IllegalArgumentException e) {
                    }
                }
            }

            Status status;
            while (true) {
                console.print(
                        "Status(FIRED, HIRED, RECOMMENDED_FOR_PROMOTION, REGULAR, PROBATION) or"
                                + " null: ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (line.isEmpty() || line.equals("null")) {
                    status = null;
                    break;
                } else {
                    try {
                        status = Status.valueOf(line);
                        break;
                    } catch (NullPointerException | IllegalArgumentException e) {
                    }
                }
            }
            Organization organization = askOrganization(console);
            return new Worker(
                    id, name, coordinates, salary, startDate, position, status, organization);
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }

    public static Coordinates askCoordinates(Console console) throws AskBreak {
        console.println("Coordinates:");
        try {
            int x;
            while (true) {
                console.print("x(int x<=266):");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.isEmpty()) {
                    try {
                        x = Integer.parseInt(line);
                        if (x <= 266) break;
                    } catch (NumberFormatException e) {
                    }
                }
            }
            Float y;
            while (true) {
                console.print("y(Float y): ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.isEmpty()) {
                    try {
                        y = Float.parseFloat(line);
                        break;
                    } catch (NumberFormatException e) {
                    }
                }
            }
            return new Coordinates(x, y);
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }

    public static Organization askOrganization(Console console) throws AskBreak {
        console.println("Organization:");
        try {
            Float annualTurnover;
            while (true) {
                console.print(
                        "annualTurnover(Float annualTurnover>0) or null for Organization null"
                                + " declaration:");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.isEmpty()) {
                    try {
                        annualTurnover = Float.parseFloat(line);
                        if (annualTurnover > 0) break;
                    } catch (NumberFormatException e) {
                    }
                } else {
                    return null;
                }
            }
            OrganizationType type;
            while (true) {
                console.print(
                        "OrganizationType (COMMERCIAL, PUBLIC, OPEN_JOINT_STOCK_COMPANY) or null:"
                                + " ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.isEmpty()) {
                    try {
                        type = OrganizationType.valueOf(line);
                        break;
                    } catch (NullPointerException | IllegalArgumentException e) {
                    }
                } else {
                    type = null;
                    break;
                }
            }
            var postalAddress = AskAddress(console);
            return new Organization(annualTurnover, type, postalAddress);
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }

    public static Address AskAddress(Console console) throws AskBreak {
        console.print("Adress:");
        var town = AskLocation(console);
        if (town == null) return null;
        try {
            String street;
            while (true) {
                console.print("street(String len(street)<=174 or null):");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (line.equals("null") || line.isEmpty()) {
                    street = null;
                    break;
                }
                try {
                    street = line;
                    if ((street.length()) <= 174) break;
                } catch (NumberFormatException e) {
                }
            }
            return new Address(street, town);
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }

    public static Location AskLocation(Console console) throws AskBreak {
        console.println("Location:");
        try {
            float x;
            while (true) {
                console.print("x(float x) or null for Address null:");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (line.equals("null") || line.isEmpty()) {
                    return null;
                }
                if (!line.isEmpty()) {
                    try {
                        x = Float.parseFloat(line);
                        break;
                    } catch (NumberFormatException e) {
                    }
                }
            }
            double y;
            while (true) {
                console.print("y(double y):");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.isEmpty()) {
                    try {
                        y = Double.parseDouble(line);
                        break;
                    } catch (NumberFormatException e) {
                    }
                }
            }
            long z;
            while (true) {
                console.print("z(long z):");
                var line = console.readln().trim();
                console.println(line);
                if (line.equals("exit")) throw new AskBreak();
                if (!line.isEmpty()) {
                    try {
                        z = Long.parseLong(line);
                        break;
                    } catch (NumberFormatException e) {
                    }
                }
            }
            String name;
            while (true) {
                console.print("name(String name not null):");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.isEmpty()) {
                    try {
                        name = line;
                        break;
                    } catch (NumberFormatException e) {
                    }
                }
            }
            return new Location(x, y, z, name);
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }
}
