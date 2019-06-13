/**
 * Automatically generated by Zserio C++ extension version 1.2.0.
 */

#ifndef DATABASE_H
#define DATABASE_H

#include <memory>
#include <string>
#include <vector>
#include <set>
#include <map>
#include <zserio/ISqliteDatabase.h>
#include <zserio/SqliteConnection.h>

#include "Table.h"


class Database : public zserio::ISqliteDatabase
{
public:
    typedef std::map<std::string, std::string> TRelocationMap;

    explicit Database(const std::string& fileName,
            const TRelocationMap& tableToDbFileNameRelocationMap = TRelocationMap());
    explicit Database(sqlite3* externalConnection,
            const TRelocationMap& tableToAttachedDbNameRelocationMap = TRelocationMap());

    ~Database();

    sqlite3* connection();

    Table& getTbl();

    virtual void createSchema();
    virtual void createSchema(const std::set<std::string>& withoutRowIdTableNamesBlackList);
    virtual void deleteSchema();

    // static constexpr const char* databaseName(); // TODO: is needed?
    static constexpr const char* databaseName = "Database";

    //static constexpr const char* static void fillTableNames(std::vector<std::string>& tableNames); // TODO: is needed?
    static constexpr std::array<const char*, 1> tableNames =
    {
        "tbl"
    };

private:
    void initTables(const TRelocationMap& tableToAttachedDbNameRelocationMap);
    void attachDatabase(const std::string& fileName, const std::string& attachedDbName);
    void detachDatabases();

    // TODO: is needed?
    //static constexpr const char* tableNameTbl();

    zserio::SqliteConnection m_db;
    std::vector<std::string> m_attachedDbList;

    std::unique_ptr<Table> m_tbl_;
};


#endif // DATABASE_H
