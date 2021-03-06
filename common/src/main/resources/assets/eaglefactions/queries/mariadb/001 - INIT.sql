
-- Create Version Table
CREATE TABLE `Version` (
  `Version` INT NOT NULL,
  PRIMARY KEY (`Version`)
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `Version_UNIQUE` ON `Version` (`Version`);

-- Create Factions Table
CREATE TABLE `Factions` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(200) NOT NULL,
  `Tag` VARCHAR(200) NOT NULL,
  `TagColor` VARCHAR(40) NULL,
  `Leader` VARCHAR(36) NOT NULL,
  `Home` VARCHAR(200) NULL,
  `LastOnline` VARCHAR(200) NOT NULL,
  `Alliances` VARCHAR(255) NOT NULL,
  `Enemies` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`, `Name`)
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `Name_UNIQUE` ON `Factions` (`Name`);

-- Create Recruits Table
CREATE TABLE `FactionRecruits` (
  `RecruitUUID` VARCHAR(36) NOT NULL,
  `FactionName` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `RecruitUUID_UNIQUE` ON `FactionRecruits` (`RecruitUUID`);

-- Create Members Table
CREATE TABLE `FactionMembers` (
  `MemberUUID` VARCHAR(36) NOT NULL,
  `FactionName` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `MemberUUID_UNIQUE` ON `FactionMembers` (`MemberUUID`);

-- Create Officers Table
CREATE TABLE `FactionOfficers` (
  `OfficerUUID` VARCHAR(36) NOT NULL,
  `FactionName` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `OfficerUUID_UNIQUE` ON `FactionOfficers` (`OfficerUUID`);

-- Create FactionAlliances Table
-- CREATE TABLE FactionAlliances (
--   FactionName  VARCHAR(200)      UNIQUE        NOT NULL,
--   AlliancesIds VARCHAR(200)                    NOT NULL,
--   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
-- );
-- CREATE UNIQUE INDEX FactionAlliances_FactionName ON FactionAlliances (FactionName);
--
-- Create FactionEnemies Table
-- CREATE TABLE FactionEnemies (
--   FactionName VARCHAR(200)        UNIQUE      NOT NULL,
--   EnemiesIds  VARCHAR(200)                    NOT NULL,
--   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
-- );
-- CREATE UNIQUE INDEX FactionEnemies_FactionName ON FactionEnemies (FactionName);

-- Create FactionTruces Table
-- CREATE TABLE `FactionTruces` (
--   `FactionName`   VARCHAR(200)                             NOT NULL,
--   `TrucesIds`  VARCHAR(200)        UNIQUE      NOT NULL,
-- );
-- CREATE UNIQUE INDEX FactionTruces_FactionName ON FactionTruces (FactionName);

-- Create LeaderFlags Table
CREATE TABLE `LeaderFlags` (
  `FactionName` VARCHAR(200) NOT NULL,
  `Use` TINYINT(1) NOT NULL,
  `Place` TINYINT(1) NOT NULL,
  `Destroy` TINYINT(1) NOT NULL,
  `Claim` TINYINT(1) NOT NULL,
  `Attack` TINYINT(1) NOT NULL,
  `Invite` TINYINT(1) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `LeaderFlags` (`FactionName`);

-- Create OfficerFlags Table
CREATE TABLE `OfficerFlags` (
  `FactionName` VARCHAR(200) NOT NULL,
  `Use` TINYINT(1) NOT NULL,
  `Place` TINYINT(1) NOT NULL,
  `Destroy` TINYINT(1) NOT NULL,
  `Claim` TINYINT(1) NOT NULL,
  `Attack` TINYINT(1) NOT NULL,
  `Invite` TINYINT(1) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `OfficerFlags` (`FactionName`);

-- Create MemberFlags Table
CREATE TABLE `MemberFlags` (
  `FactionName` VARCHAR(200) NOT NULL,
  `Use` TINYINT(1) NOT NULL,
  `Place` TINYINT(1) NOT NULL,
  `Destroy` TINYINT(1) NOT NULL,
  `Claim` TINYINT(1) NOT NULL,
  `Attack` TINYINT(1) NOT NULL,
  `Invite` TINYINT(1) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `MemberFlags` (`FactionName`);

-- Create RecruitFlags Table
CREATE TABLE `RecruitFlags` (
  `FactionName` VARCHAR(200) NOT NULL,
  `Use` TINYINT(1) NOT NULL,
  `Place` TINYINT(1) NOT NULL,
  `Destroy` TINYINT(1) NOT NULL,
  `Claim` TINYINT(1) NOT NULL,
  `Attack` TINYINT(1) NOT NULL,
  `Invite` TINYINT(1) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `RecruitFlags` (`FactionName`);

-- Create AllyFlags Table
CREATE TABLE `AllyFlags` (
  `FactionName` VARCHAR(200) NOT NULL,
  `Use` TINYINT(1) NOT NULL,
  `Place` TINYINT(1) NOT NULL,
  `Destroy` TINYINT(1) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `AllyFlags` (`FactionName`);

-- Create Claims Table
CREATE TABLE `Claims` (
  `FactionName` VARCHAR(200) NOT NULL,
  `WorldUUID` VARCHAR(36) NOT NULL,
  `ChunkPosition` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;

-- Create FactionsChest Table
CREATE TABLE `FactionChests` (
  `FactionName` VARCHAR(200) NOT NULL,
  `ChestItems` BLOB NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `FactionChests` (`FactionName`);

-- Create Players Table
CREATE TABLE `Players` (
  `PlayerUUID` VARCHAR(36) NOT NULL,
  `Name` VARCHAR(200) NOT NULL,
  `Power` FLOAT NOT NULL,
  `MaxPower` FLOAT NOT NULL,
  `DeathInWarzone` TINYINT(1) NOT NULL,
  PRIMARY KEY (`PlayerUUID`)
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `PlayerUUID_UNIQUE` ON `Players` (`PlayerUUID`);

-- Set database version to 1
INSERT INTO Version VALUES (1);