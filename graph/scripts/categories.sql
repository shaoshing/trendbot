

-- Get level1 categories ids and titles
SELECT page.page_id, ",", page.page_title
FROM page
WHERE page.page_id IN (1004110,1013214,1633936,1784082,2389032,24980271,3103170,4892515,690747,
                        691008,691182,691928,692348,693555,693708,693800,694861,694871,695027,696603,
                        696648,696763,751381,771152,8017451,956054)

-- Get level2 categories ids and titles
SELECT DISTINCT(p2.page_id), ", ", p2.page_title
FROM categorylinks
JOIN page ON categorylinks.cl_to = page.page_title AND page.page_namespace = 14
JOIN page as p2 ON p2.page_id = categorylinks.cl_from
WHERE cl_to IN ("Mathematics","People","Science","Law","Medicine","History","Sports","Geography",
                "Culture","Agriculture","Politics","Nature","Technology","Education","Health",
                "Business","Belief","Humanities","Chronology","Society","Humans","Life","Environment"
                ,"Arts","Language","Concepts")
  AND cl_type = "subcat";
