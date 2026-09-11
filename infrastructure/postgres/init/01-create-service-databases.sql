CREATE DATABASE openefms_auth;
CREATE DATABASE openefms_ingestion;
CREATE DATABASE openefms_fraud;
CREATE DATABASE openefms_explainable_ai;
CREATE DATABASE openefms_rules;
CREATE DATABASE openefms_lists;
CREATE DATABASE openefms_cases;

GRANT ALL PRIVILEGES ON DATABASE openefms_auth TO openefms;
GRANT ALL PRIVILEGES ON DATABASE openefms_ingestion TO openefms;
GRANT ALL PRIVILEGES ON DATABASE openefms_fraud TO openefms;
GRANT ALL PRIVILEGES ON DATABASE openefms_explainable_ai TO openefms;
GRANT ALL PRIVILEGES ON DATABASE openefms_rules TO openefms;
GRANT ALL PRIVILEGES ON DATABASE openefms_lists TO openefms;
GRANT ALL PRIVILEGES ON DATABASE openefms_cases TO openefms;
