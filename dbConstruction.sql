--
-- File generated with SQLiteStudio v3.0.6 on Mi Okt 28 13:40:59 2015
--
-- Text encoding used: windows-1252
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Table: ldaConfigurations
CREATE TABLE ldaConfigurations (ldaConfigurationID INTEGER PRIMARY KEY AUTOINCREMENT, alpha DOUBLE, kappa INTEGER, eta DOUBLE, UNIQUE (alpha, kappa, eta))

-- Table: topicDistances
CREATE TABLE topicDistances (ldaConfigurationID_1 INT, ldaConfigurationID_2 INT, topicID_1 INT, topicID_2 INT, distance DOUBLE, PRIMARY KEY (ldaConfigurationID_1, ldaConfigurationID_2, topicID_1, topicID_2) ON CONFLICT REPLACE, CONSTRAINT topicDistances_ldaConfig1_FK FOREIGN KEY (ldaConfigurationID_1) REFERENCES ldaConfigurations (ldaConfigurationID), CONSTRAINT topicDistances_ldaConfig2_FK FOREIGN KEY (ldaConfigurationID_2) REFERENCES ldaConfigurations (ldaConfigurationID))

-- Table: keywords
CREATE TABLE keywords (keywordID INTEGER PRIMARY KEY AUTOINCREMENT, keyword VARCHAR (50) UNIQUE)

-- Table: topics
CREATE TABLE topics (topicID INTEGER, ldaConfigurationID INTEGER, PRIMARY KEY (topicID, ldaConfigurationID), FOREIGN KEY (ldaConfigurationID) REFERENCES ldaConfigurations (ldaConfigurationID))

-- Table: datasetDistances
CREATE TABLE datasetDistances (ldaConfigurationID_1 INT REFERENCES ldaConfigurations (ldaConfigurationID), ldaConfigurationID_2 INT REFERENCES ldaConfigurations (ldaConfigurationID), distance DOUBLE, PRIMARY KEY (ldaConfigurationID_1 ASC, ldaConfigurationID_2 ASC) ON CONFLICT REPLACE)

-- Table: keywordInTopic
CREATE TABLE keywordInTopic (topicID INTEGER, keywordID INTEGER, probability DOUBLE, ldaConfigurationID INTEGER, PRIMARY KEY (topicID, keywordID, ldaConfigurationID) ON CONFLICT REPLACE, FOREIGN KEY (topicID, ldaConfigurationID) REFERENCES topics (topicID, ldaConfigurationID))

-- Index:  combinedLDAConfigurationIndex
CREATE INDEX " combinedLDAConfigurationIndex" ON datasetDistances (ldaConfigurationID_1 ASC, ldaConfigurationID_2 ASC)

-- Index: topicDistances_ldaConfigurationIDPairs_index
CREATE INDEX topicDistances_ldaConfigurationIDPairs_index ON topicDistances (ldaConfigurationID_1 ASC, ldaConfigurationID_2 ASC)

-- Index: ldaConfigIndex
CREATE INDEX ldaConfigIndex ON topics (ldaConfigurationID DESC)

-- Index: topicDistances_topicIDPairs_index
CREATE INDEX topicDistances_topicIDPairs_index ON topicDistances (topicID_1 ASC, topicID_2 ASC)

-- Index: keywordText
CREATE INDEX keywordText ON keywords (keyword DESC)

-- Index: paramIndex
CREATE INDEX paramIndex ON ldaConfigurations (alpha DESC, kappa DESC, eta DESC)

-- Index: keywordID_KIT_index
CREATE INDEX keywordID_KIT_index ON keywordInTopic (keywordID ASC)

-- Index: ldaConfigurationID_KIT_index
CREATE INDEX ldaConfigurationID_KIT_index ON keywordInTopic (ldaConfigurationID ASC)

COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
