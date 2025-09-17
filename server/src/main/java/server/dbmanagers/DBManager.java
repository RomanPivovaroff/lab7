package server.dbmanagers;

import common.entity.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Vector;

public class DBManager {
    private Connection con;

    public DBManager() {
        try {
            con =
                    DriverManager.getConnection(
                            "jdbc:postgresql://db:5432/studs",
                            System.getenv("USERNAME"),
                            System.getenv("PASSWORD"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized Vector<Worker> readAllWorkers() {
        Vector<Worker> workers = new Vector<>();
        String sql = "select * from collection";
        try (var ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String position = rs.getString("position");
                String status = rs.getString("status");
                String annualTurnover = rs.getString("organization_annual_turnover");
                String locationName = rs.getString("location_name");
                String type = rs.getString("organization_organization_type");
                Worker worker = new Worker();
                worker.setId(rs.getInt("id"));
                worker.setName(rs.getString("name"));
                worker.setSalary(rs.getInt("salary"));
                worker.setPosition(position == null ? null : Position.valueOf(position.toUpperCase()));
                worker.setCoordinates(
                        new Coordinates(rs.getInt("coordinate_x"), rs.getFloat("coordinate_y")));
                worker.setOrganization(
                        annualTurnover == null
                                ? null
                                : new Organization(
                                        Float.parseFloat(annualTurnover),
                                        type == null ? null : OrganizationType.valueOf(type.toUpperCase()),
                                        locationName == null
                                                ? null
                                                : new Address(
                                                        rs.getString("address_street"),
                                                        new Location(
                                                                rs.getFloat("location_x"),
                                                                rs.getDouble("location_y"),
                                                                rs.getLong("location_z"),
                                                                locationName))));
                worker.setStatus(status == null ? null : Status.valueOf(status.toUpperCase()));
                worker.setStartDate(ZonedDateTime.parse(rs.getString("start_date")));
                worker.setCreator(rs.getString("who_created"));
                worker.setCreationDate((Date) rs.getObject("creation_date"));
                workers.add(worker);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workers;
    }

    public synchronized boolean write(Worker worker) {
        String insert =
                """
INSERT INTO Collection(name, salary, start_date, coordinate_x, coordinate_y, status, position, organization_annual_turnover, organization_organization_type, address_street, location_x, location_y, location_z, location_name, creation_date, who_created)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);""";
        try (var psInsert = con.prepareStatement(insert)) {
            psInsert.setString(1, worker.getName());
            psInsert.setLong(2, worker.getSalary());
            psInsert.setString(3, worker.getStartDate().toString());
            psInsert.setInt(4, worker.getCoordinates().getX());
            psInsert.setFloat(5, worker.getCoordinates().getY());
            if (worker.getStatus() == null) psInsert.setNull(6, Types.VARCHAR);
            else psInsert.setString(6, worker.getStatus().toString());
            psInsert.setString(7, worker.getPosition().toString());
            if (worker.getOrganization() == null) {
                psInsert.setNull(8, Types.VARCHAR);
                psInsert.setNull(9, Types.VARCHAR);
                psInsert.setNull(10, Types.VARCHAR);
                psInsert.setNull(11, Types.FLOAT);
                psInsert.setNull(12, Types.DOUBLE);
                psInsert.setNull(13, Types.BIGINT);
                psInsert.setNull(14, Types.VARCHAR);
            } else {
                psInsert.setString(8, worker.getOrganization().getAnnualTurnover().toString());
                if (worker.getOrganization().getType() == null) psInsert.setNull(9, Types.VARCHAR);
                else psInsert.setString(9, worker.getOrganization().getType().toString());
                if (worker.getOrganization().getPostalAdress() == null) {
                    Address address = worker.getOrganization().getPostalAdress();
                    if (address.getStreet() == null) psInsert.setNull(10, Types.VARCHAR);
                    else psInsert.setString(10, address.getStreet());
                    psInsert.setFloat(11, address.getTown().getX());
                    psInsert.setDouble(12, address.getTown().getY());
                    psInsert.setLong(13, address.getTown().getZ());
                    psInsert.setString(14, address.getTown().getName());
                } else {
                    psInsert.setNull(10, Types.VARCHAR);
                    psInsert.setNull(11, Types.FLOAT);
                    psInsert.setNull(12, Types.DOUBLE);
                    psInsert.setNull(13, Types.BIGINT);
                    psInsert.setNull(14, Types.VARCHAR);
                }
            }
            psInsert.setDate(15, new java.sql.Date(worker.getCreationDate().getTime()));
            psInsert.setString(16, worker.getCreator());
            psInsert.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean remove(int id) {
        String remove =
                """
                DELETE FROM Collection WHERE id = ?;
                """;
        try (var psRemove = con.prepareStatement(remove)) {
            psRemove.setInt(1, id);
            psRemove.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean removeAll() {
        String removeAll =
                """
                TRUNCATE TABLE Collection;
                """;
        try (var psRemove = con.prepareStatement(removeAll)) {
            psRemove.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean update(Worker worker) {
        String update =
                """
                UPDATE Collection
                SET name = ?,
                salary = ?,
                start_date = ?,
                coordinate_x = ?,
                coordinate_y = ?,
                status = ?,
                position = ?,
                organization_annual_turnover = ?,
                organization_organization_type = ?,
                address_street = ?,
                location_x = ?,
                location_y = ?,
                location_z = ?,
                location_name = ?,
                WHERE id = ?;
                """;
        try (var psInsert = con.prepareStatement(update)) {
            psInsert.setString(1, worker.getName());
            psInsert.setLong(2, worker.getSalary());
            psInsert.setString(3, worker.getStartDate().toString());
            psInsert.setInt(4, worker.getCoordinates().getX());
            psInsert.setFloat(5, worker.getCoordinates().getY());
            if (worker.getStatus() == null) psInsert.setNull(6, Types.VARCHAR);
            else psInsert.setString(6, worker.getStatus().toString());
            psInsert.setString(7, worker.getPosition().toString());
            if (worker.getOrganization() == null) {
                psInsert.setNull(8, Types.VARCHAR);
                psInsert.setNull(9, Types.VARCHAR);
                psInsert.setNull(10, Types.VARCHAR);
                psInsert.setNull(11, Types.FLOAT);
                psInsert.setNull(12, Types.DOUBLE);
                psInsert.setNull(13, Types.BIGINT);
                psInsert.setNull(14, Types.VARCHAR);
            } else {
                psInsert.setString(8, worker.getOrganization().getAnnualTurnover().toString());
                if (worker.getOrganization().getType() == null) psInsert.setNull(9, Types.VARCHAR);
                else psInsert.setString(9, worker.getOrganization().getType().toString());
                if (worker.getOrganization().getPostalAdress() == null) {
                    Address address = worker.getOrganization().getPostalAdress();
                    if (address.getStreet() == null) psInsert.setNull(10, Types.VARCHAR);
                    else psInsert.setString(10, address.getStreet());
                    psInsert.setFloat(11, address.getTown().getX());
                    psInsert.setDouble(12, address.getTown().getY());
                    psInsert.setLong(13, address.getTown().getZ());
                    psInsert.setString(14, address.getTown().getName());
                } else {
                    psInsert.setNull(10, Types.VARCHAR);
                    psInsert.setNull(11, Types.FLOAT);
                    psInsert.setNull(12, Types.DOUBLE);
                    psInsert.setNull(13, Types.BIGINT);
                    psInsert.setNull(14, Types.VARCHAR);
                }
            }
            psInsert.setInt(15, worker.getId());
            psInsert.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean CreateTable(String scriptFileName) {
        if (scriptFileName == null) {
            return false;
        }
        String sql = null;
        try {
            sql = Files.readString(Paths.get(scriptFileName));
        } catch (IOException e) {
            return false;
        }
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            String[] commands = sql.split(";\\s*");
            for (String command : commands) {
                String tcom = command.trim();
                if (!tcom.isEmpty()) {
                    stmt.execute(tcom);
                }
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
}
