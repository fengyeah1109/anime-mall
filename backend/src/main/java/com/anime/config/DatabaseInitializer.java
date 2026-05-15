package com.anime.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final DataSource dataSource;

    @PostConstruct
    public void init() {
        fixArticleCoverImageColumn();
        fixAfterSaleTableColumns();
        initDefaultAdmin();
    }

    private void fixArticleCoverImageColumn() {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "ALTER TABLE article MODIFY COLUMN cover_image VARCHAR(1000)";
            conn.createStatement().execute(sql);
            System.out.println("Successfully modified article.cover_image column to VARCHAR(1000)");
        } catch (SQLException e) {
            System.out.println("Article column fix attempt: " + e.getMessage());
        }
    }

    private void fixAfterSaleTableColumns() {
        try (Connection conn = dataSource.getConnection()) {
            // 检查表是否存在，不存在则创建
            String checkTableSql = "SHOW TABLES LIKE 'after_sale'";
            ResultSet rs = conn.createStatement().executeQuery(checkTableSql);
            if (!rs.next()) {
                // 创建表
                String createTableSql = """
                    CREATE TABLE after_sale (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        order_item_id BIGINT NOT NULL COMMENT '订单商品项ID',
                        user_id BIGINT NOT NULL COMMENT '用户ID',
                        type TINYINT NOT NULL COMMENT '1-退货退款 2-仅退款 3-换货',
                        reason VARCHAR(200) NOT NULL COMMENT '申请原因',
                        description VARCHAR(500) DEFAULT NULL COMMENT '问题描述',
                        evidence_images VARCHAR(1000) DEFAULT NULL COMMENT '凭证图片，逗号分隔',
                        status TINYINT DEFAULT 0 COMMENT '0-待审核 1-审核通过 2-审核拒绝 3-已完成',
                        apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                        handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
                        reply VARCHAR(500) DEFAULT NULL COMMENT '管理员回复',
                        deleted INT DEFAULT 0 COMMENT '是否删除 0-否 1-是'
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后申请表'
                    """;
                conn.createStatement().execute(createTableSql);
                System.out.println("Successfully created after_sale table");
            } else {
                // 检查并添加缺失的列
                checkAndAddColumn(conn, "after_sale", "order_item_id", "BIGINT NOT NULL COMMENT '订单商品项ID'");
                checkAndAddColumn(conn, "after_sale", "user_id", "BIGINT NOT NULL COMMENT '用户ID'");
                checkAndAddColumn(conn, "after_sale", "type", "TINYINT NOT NULL COMMENT '1-退货退款 2-仅退款 3-换货'");
                checkAndAddColumn(conn, "after_sale", "reason", "VARCHAR(200) NOT NULL COMMENT '申请原因'");
                checkAndAddColumn(conn, "after_sale", "description", "VARCHAR(500) DEFAULT NULL COMMENT '问题描述'");
                checkAndAddColumn(conn, "after_sale", "evidence_images", "VARCHAR(1000) DEFAULT NULL COMMENT '凭证图片，逗号分隔'");
                checkAndAddColumn(conn, "after_sale", "status", "TINYINT DEFAULT 0 COMMENT '0-待审核 1-审核通过 2-审核拒绝 3-已完成'");
                checkAndAddColumn(conn, "after_sale", "apply_time", "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间'");
                checkAndAddColumn(conn, "after_sale", "handle_time", "DATETIME DEFAULT NULL COMMENT '处理时间'");
                checkAndAddColumn(conn, "after_sale", "reply", "VARCHAR(500) DEFAULT NULL COMMENT '管理员回复'");
                checkAndAddColumn(conn, "after_sale", "deleted", "INT DEFAULT 0 COMMENT '是否删除 0-否 1-是'");
                
                // 移除旧的列（如果存在）
                removeColumnIfExists(conn, "after_sale", "order_id");
                removeColumnIfExists(conn, "after_sale", "images");
                removeColumnIfExists(conn, "after_sale", "amount");
                removeColumnIfExists(conn, "after_sale", "admin_id");
                removeColumnIfExists(conn, "after_sale", "admin_remark");
                removeColumnIfExists(conn, "after_sale", "create_time");
                removeColumnIfExists(conn, "after_sale", "update_time");
                
                System.out.println("Successfully updated after_sale table columns");
            }
        } catch (SQLException e) {
            System.out.println("AfterSale table fix attempt: " + e.getMessage());
        }
    }

    private void checkAndAddColumn(Connection conn, String tableName, String columnName, String columnDefinition) throws SQLException {
        String checkSql = "SHOW COLUMNS FROM " + tableName + " LIKE '" + columnName + "'";
        ResultSet rs = conn.createStatement().executeQuery(checkSql);
        if (!rs.next()) {
            String addSql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
            try {
                conn.createStatement().execute(addSql);
                System.out.println("Added column " + columnName + " to " + tableName);
            } catch (SQLException e) {
                System.out.println("Failed to add column " + columnName + ": " + e.getMessage());
            }
        }
    }

    private void removeColumnIfExists(Connection conn, String tableName, String columnName) {
        try {
            String checkSql = "SHOW COLUMNS FROM " + tableName + " LIKE '" + columnName + "'";
            ResultSet rs = conn.createStatement().executeQuery(checkSql);
            if (rs.next()) {
                String dropSql = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;
                conn.createStatement().execute(dropSql);
                System.out.println("Removed column " + columnName + " from " + tableName);
            }
        } catch (SQLException e) {
            System.out.println("Remove column attempt for " + columnName + ": " + e.getMessage());
        }
    }

    private void initDefaultAdmin() {
        try (Connection conn = dataSource.getConnection()) {
            Long existingId = null;
            String checkSql = "SELECT id FROM admin WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, "admin");
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    existingId = rs.getLong("id");
                }
            }

            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            String hashedPassword = encoder.encode("admin123");

            if (existingId == null) {
                String insertSql = "INSERT INTO admin (username, password, real_name, role, status, create_time) VALUES (?, ?, ?, ?, ?, NOW())";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, hashedPassword);
                    pstmt.setString(3, "系统管理员");
                    pstmt.setInt(4, 1);
                    pstmt.setInt(5, 1);
                    int affected = pstmt.executeUpdate();
                    if (affected > 0) {
                        System.out.println("Successfully created default admin account (username: admin, password: admin123)");
                    }
                }
            } else {
                String updateSql = "UPDATE admin SET password = ?, real_name = ?, role = ?, status = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(1, hashedPassword);
                    pstmt.setString(2, "系统管理员");
                    pstmt.setInt(3, 1);
                    pstmt.setInt(4, 1);
                    pstmt.setLong(5, existingId);
                    int affected = pstmt.executeUpdate();
                    if (affected > 0) {
                        System.out.println("Successfully updated default admin account (username: admin, password: admin123)");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Admin initialization attempt: " + e.getMessage());
        }
    }
}
