package com.dorm.db;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DatabaseSetup {
    public static void initialize() throws IOException, SQLException {
        try (InputStream in = DatabaseSetup.class.getResourceAsStream("/schema.sql")) {
            if (in == null) throw new IOException("schema.sql is missing.");
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement s = DatabaseConnection.getInstance().getConnection().createStatement()) {
                s.execute(sql);
            }
        }
    }
}
