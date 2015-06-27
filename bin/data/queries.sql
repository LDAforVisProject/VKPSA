select lda.ldaConfigurationID, lda.alpha, lda.kappa, lda.eta, topicID, keyword, probability from keywordInTopic kit
join keywords kw on kw.keywordID = kit.keywordID
join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID
order by lda.ldaConfigurationID, topicID;

select count(*) from keywordInTopic kit
join keywords kw on kw.keywordID = kit.keywordID
join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID
order by lda.ldaConfigurationID, topicID;

select lda.ldaConfigurationID, lda.alpha, lda.kappa, lda.eta, topicID, count(*) from keywordInTopic kit
join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID
group by lda.ldaConfigurationID, lda.alpha, lda.kappa, lda.eta, topicID
order by lda.ldaConfigurationID, topicID;

select * from ldaConfigurations lda
order by lda.ldaConfigurationID;

select count(*) as numKeywords from keywords;

delete from keywordInTopic;
delete from topics;
delete from ldaConfigurations;