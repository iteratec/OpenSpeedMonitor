--  Lock Database
--  Changeset next-release-SCHEME.groovy::1456309666028-2::bwo (generated)::(Checksum: 3:aff669638d8390257c6dec6410e72f46)
ALTER TABLE `userspecific_csi_dashboard` ADD `csi_type_visually_complete` bit NOT NULL;

--  Changeset next-release-SCHEME.groovy::1456309666028-1::bwo (generated)::(Checksum: 3:2e79e84d46ce84a4ca923a2a7f66d059)
ALTER TABLE `userspecific_csi_dashboard` ADD `csi_type_doc_complete` bit NOT NULL;

