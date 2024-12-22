package driver;

import java.io.*;
import java.sql.*;
import java.util.*;
import function.*;

public class MyStatement implements Statement {

    private final PrintWriter out;
    private final BufferedReader in;
    private boolean closed;
    private String database;

    /**
     * Constructeur pour MyStatement
     *
     * @param out Le flux pour envoyer des requêtes au serveur.
     * @param in  Le flux pour recevoir des réponses du serveur.
     */


    public MyStatement(PrintWriter out, BufferedReader in,String database) {
        this.out = out;
        this.in = in;
        this.closed = false;
        this.database = database;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        if (closed) {
            throw new SQLException("Le Statement est fermé.");
        }

        try {
            out.println("BDD : "+database);

            out.println(sql);
            System.out.println("Requête envoyée : " + sql);

            String response = Function.readMultiLineResponse(in);

            if (response.equals("") || response.isEmpty()) {
                throw new SQLException("Aucune réponse du serveur.");
            }

            if (response.contains("+") && response.contains("-") && response.contains("|")){
                List <String> attributs = Function.extractHeadersFromString(response);
                List <List <Object>> values = Function.extractDataFromString(response);
                return new MyResultSet(attributs,values);
            } else {
                System.out.println(response);
            }

            return new MyResultSet(response);
        } catch (IOException e) {
            throw new SQLException("Erreur lors de l'exécution de la requête : " + e.getMessage(), e);
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        if (closed) {
            throw new SQLException("Le Statement est fermé.");
        }

        try {
            // Envoyer la requête au serveur
            out.println(sql);
            System.out.println("Requête envoyée : " + sql);

            // Lire la réponse depuis le serveur
            String response = in.readLine();
            if (response == null) {
                throw new SQLException("Aucune réponse du serveur.");
            }

            // Retourner un résultat (par exemple, le nombre de lignes affectées)
            return Integer.parseInt(response.trim());
        } catch (IOException | NumberFormatException e) {
            throw new SQLException("Erreur lors de l'exécution de la requête : " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws SQLException {
        this.closed = true;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    // Méthodes non implémentées pour l'instant (simples stubs)
    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {}

    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {}

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {}

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {}

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {}

    @Override
    public void cancel() throws SQLException {}

    @Override
    public void setCursorName(String name) throws SQLException {}

    @Override
    public boolean execute(String sql) throws SQLException {
        throw new UnsupportedOperationException("Non implémenté.");
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return -1;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {}

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {}

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException {}

    @Override
    public void clearBatch() throws SQLException {}

    @Override
    public int[] executeBatch() throws SQLException {
        return new int[0];
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {}

    @Override
    public void closeOnCompletion() throws SQLException {}

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isWrapperFor'");
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unwrap'");
    }
}
