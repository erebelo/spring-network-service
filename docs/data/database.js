// CREATE AND SWITCH TO THE DATABASE
use network_db;

// CREATE COLLECTIONS
db.createCollection("contracts");
db.createCollection("organizations");
db.createCollection("relationships");
db.createCollection("non_selling_relationships");

// CONTRACTS INDEXES
db.contracts.createIndex({ referenceId: 1 }, { unique: true });
db.contracts.createIndex({ role: 1 });

// ORGANIZATIONS INDEXES
db.organizations.createIndex({ orgRefId: 1 }, { unique: true });

// RELATIONSHIPS INDEXES
db.relationships.createIndex( "from.referenceId": 1 });
db.relationships.createIndex({ "to.referenceId": 1 });

// NON_SELLING_RELATIONSHIPS INDEXES
db.non_selling_relationships.createIndex({ "from.orgId": 1 });
db.non_selling_relationships.createIndex({ "to.referenceId": 1 });	