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

select lda.ldaConfigurationID, topicID, keyword, probability from keywordInTopic kit
join keywords kw on kw.keywordID = kit.keywordID
join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID
where 
    lda.ldaConfigurationID = 150 and
    topicID = 1
order by probability desc, topicID;

select count(*) from topics t
join ldaConfigurations lda on t.ldaConfigurationID = lda.ldaConfigurationID
where lda.ldaConfigurationID = 150;
 
select * from ldaConfigurations
where ldaConfigurationID = 151
