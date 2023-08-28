import os
import csv
import requests

from selenium import webdriver
from selenium.webdriver.common.by import By

import spacy
from spacy import displacy

curr_path = os.path.join(os.getcwd(), 'Maharashtra')
file_target = 'content3_data.csv'


def traverse(text, head_graph, token_l, curr_id):
    direct_children = []
    overall = []
    children = []
    result = []

    direct_children = head_graph[curr_id]
    list_id = [0]

    def dfs(curr, arr):
        if (curr not in children) and (token_l[curr]['pos'] != 'SPACE'):
            if token_l[curr]['dep'] != 'cc':
                children.append([curr, ent_graph[curr], list_id[0]] if curr in ent_graph else [curr, None, list_id[0]])

        for c in arr:
            # if flag_cc is False:
            if True:
                if ((token_l[c]['dep'] == 'compound') or (token_l[c]['dep'] == 'conj') or
                        (token_l[c]['dep'] == 'appos') or (token_l[c]['dep'] == 'amod')) and \
                        (token_l[c]['pos'] != 'SPACE'):
                    dfs(c, head_graph[c])

            if True:
                # if (token_l[c]['dep'] == 'conj') and (token_l[c]['pos'] != 'SPACE'):
                if (token_l[c]['dep'] == 'cc') and (token_l[c]['pos'] != 'SPACE'):
                    list_id[0] += 1
                    dfs(c, head_graph[c])

    for direct_children_index in direct_children:
        dfs(direct_children_index, head_graph[direct_children_index])
        children.sort()

        result.append(children)

    result_t = []
    for item in result:
        if item not in result_t:
            result_t.append(item)

    result = result_t
    del result_t

    # print(result)

    if len(result) != 0:
        return result[0]

    else:
        return None

# with open(os.path.join(curr_path, file_target), 'a') as csvfile:
#     csvwriter = csv.writer(csvfile)
#     csvwriter.writerow(['sno', 'data', 'link', 'date'])

place_prep = ['in', 'on', 'at', 'by', 'under', 'over', 'beside', 'between', 'among', 'above', 'below', 'behind',
              'in-front-of', 'near']
woi_cue = ['dead', 'injured', 'killed', 'wounded', 'missing', 'killing', 'injuries', 'injuring', 'injure', 'kill',
           'wound', 'injury', 'victims',

           'accident', 'crash', 'hit', 'smash', 'crush', 'collision', 'accidents', 'mishap', 'explosion', 'crashes',
           'crashed', 'collapsed', 'wreck', 'knocked',

           'cycling', 'biking', 'driving', 'motorcycling', 'racing', 'gliding', 'riding', 'walking', 'strolling',
           'roaming']
roads = ["road", "highway", "expressway", "street", "lane", "avenue", "boulevard", "marg", "flyover", "walkover",
         "expy", "rd", "galli", "gali", "hwy", "rasta", "maarg", "path"]

def is_road(sentence, nlp):
    for word_sent in sentence:
        for word_road in roads:
            if nlp(word_sent).similarity(nlp(word_road)) > 0.5:
                return True

    return False

def get_coords(sentence):
    q = "https://apihub.latlong.ai/v4/geocode.json?address=" + sentence
    r = requests.get(q, headers={
        'X-Authorization-Token': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJUb2tlbklEIjoiY2Q2ZmEzZjQtNjI2NC00Yjc5LWEwMjctOTNkZWUwZjdjMzYxIiwiQ2xpZW50SUQiOiIxNGI0MTVhYS01NjVkLTRjZWEtYjFlNC05ZTJiNTU0NmEzMWYiLCJCdW5pdElEIjoxMDMxNCwiQXBwTmFtZSI6InNvYXJkcihzb2FyZC5yYXNwaUBnbWFpbC5jb20pIC0gU2lnbiBVcCIsIkFwcElEIjoxMDI4MiwiVGltZVN0YW1wIjoiMjAyMy0wOC0xNiAxMDo0NjoxMyIsImV4cCI6MTY5NDc3NDc3M30.1xVUOfkoj5nl9Vvrtx6BBXV0ntusYBmqd4iSl53-wgk'})

    r = r.json()
    return [r['data']['longitude'], r['data']['latitude']]

driver = webdriver.Chrome('./chromedriver')

with open(os.path.join(curr_path, 'content3.csv'), mode='r') as file:
    nlp = spacy.load('en_core_web_md')

    csvFile = csv.reader(file)
    csvFile = list(csvFile)

    for content_i in range(1089, len(csvFile)):
    # for content_i in range(1, 2):
        sno, _c_, link, date = csvFile[content_i]

        driver.get(link)
        try:
        # if True:
            content = driver.find_elements(By.XPATH, '//div[@class="_s30J clearfix  "]')[0]
            text = content.get_attribute('innerText')

            try:
                extra = driver.find_elements(By.XPATH, '//div[@class="coronaaswidget"]')[0].get_attribute('innerText')

                # print("extra:", extra)

                if (extra.strip() != '') and (extra in text):
                    text = text[:text.index(extra)]

            except Exception as e:
                print("error in extra, but text intact:", text)

            # print("text:", text)

            # text = """Nashik: Three bikers were killed in two accidents in Nashik city and Chandwad on Sunday. Motorists
            # responsible for both the accidents escaped from the scene and are booked on the charge of causing death due
            # to negligence by the police.In Chandwad, three people were riding on a bike to Ganur village at around 8 pm,
            # when they were hit by a car coming from the opposite direction. The police said that the car driver was on
            # the wrong side of the road while trying to overtake a car ahead of him, when he ended up hitting the bikers.
            # All the three bikers suffered serious injuries in the accident. Two of them, Balu Devram Jadhav (53) and
            # Arun Uttam Gangurde (31) of Ganur village succumbed to the injuries whereas Sagar Thombre (25) of Parsul
            # village, who was riding the bike escaped with injuries on his legs.In another incident, Bhimkumar Singh of
            # Pathardi Phata was killed after he was hit by an unidentified motorist at around 11.30 am in Ambad
            # industrial area. Police said the victim was returning home when he met with the accident. Singh suffered
            # from serious injuries to his stomach and succumbed to them."""

            doc = nlp(text)

            collection = {}

            token_l = doc.to_json()['tokens']
            ent_graph = {}

            count = 0
            for token in doc:
                if token.ent_type_ != '':
                    ent_graph[count] = token.ent_type_

                    # print(token, ":", token.ent_type_)

                count += 1

            print("ent_graph:", ent_graph)

            head_graph = {i: [] for i in range(len(token_l))}
            accident_list = []

            for token_info in token_l:
                if (token_info['dep'] != 'punct') and (token_info['dep'] != 'ROOT') and (token_info['pos'] != 'morph'):
                    head_graph[token_info['head']].append(token_info['id'])

            for token in token_l:
                if token['pos'] == 'ADP':
                    curr_word = text[token['start']:token['end']]
                    curr_word_head = text[token_l[token['head']]['start']:token_l[token['head']]['end']]
                    prep_curr = token['id']

                    for word in place_prep:
                        if nlp(word).similarity(nlp(curr_word)) > 0.7:
                            flag_poi = False

                            for word_oi in woi_cue:
                                if flag_poi is False:
                                    if nlp(curr_word_head).similarity(nlp(word_oi)) > 0.5:
                                        flag_poi = True

                            if flag_poi is not False:
                                collection[prep_curr] = {(curr_word_head, token_l[token['head']]['id']):
                                                             traverse(text, head_graph, token_l, prep_curr)}

            print("head_graph:", head_graph)
            print("token_l:", token_l)
            print("collection:", collection)

            collection_t = {}

            for key_t in collection:
                dict_temp = collection[key_t]

                for k_t in dict_temp:
                    arr = dict_temp[k_t]

                if arr:
                    flag_present = False
                    max_length = 0

                    for token_index, token_entity, __ in arr:
                        if flag_present is False:
                            if (token_entity == 'ORG') or (token_entity == 'GPE') or (token_entity == 'FAC') or \
                                    (token_entity == 'LOC') or (token_entity == 'PRODUCT') or (token_entity == 'NORP') \
                                    or (token_entity == 'PERSON'):
                                flag_present = True

                        max_length += 1

                    # print("arr:", arr)
                    # print("max_length:", max_length)

                    if flag_present is True:
                        # final_sent = ""
                        final_sents = []

                        for ____ in range(max_length):
                            final_sents.append("")

                        for token_index, _, ___ in arr:
                            final_sents[___] += text[token_l[token_index]['start']:token_l[token_index]['end']] + " "

                        for final_sents_i in range(len(final_sents)):
                            final_sents[final_sents_i] = str(final_sents[final_sents_i]).strip()

                        for final_sents_i in range(len(final_sents)):
                             final_sents[final_sents_i] = text[token_l[key_t]['start']:token_l[key_t]['end']] + " " + \
                                                          final_sents[final_sents_i]

                        # collection_t[key] = [text[token_l[key]['start']:token_l[key]['end']] + " " + final_sent[:-1], arr]
                        collection_t[key_t] = {k_t: [final_sents, arr]}

            collection = collection_t
            del collection_t

            print("collection_t:", collection)

            result_csv = []

            collection_tt = {}

            for key in collection:
                dict_temp = collection[key]
                collection_tt[key] = [[], []]

                # print(dict_temp)

                for k_t in dict_temp:
                    arr_temp = dict_temp[k_t]
                    sentences = arr_temp[0]
                    ents = arr_temp[1]

                    # print(sentences, ents)

                    for arr_index in range(len(sentences)):
                        if (len(sentences[arr_index].split()) > 1) and \
                                ((ents[arr_index][1] == 'ORG') or (ents[arr_index][1] == 'GPE') or
                                 (ents[arr_index][1] == 'FAC') or (ents[arr_index][1] == 'LOC') or
                                 (ents[arr_index][1] == 'PRODUCT') or (ents[arr_index][1] == 'NORP') or
                                 (ents[arr_index][1] == 'PERSON')):
                            collection_tt[key][0].append(sentences[arr_index])
                            collection_tt[key][1].append(ents[arr_index])



                # arr_result = collection[key][k_t][0]
                # result_csv.append(arr_result)
                #
                # # arr_result = collection[key][0]
                # #
                # # for sent in arr_result:
                # #     result_csv.append(sent)

            collection = {key: value for key, value in collection_tt.items() if len(value[0]) != 0}
            del collection_tt

            print("collection final:", collection)

            for key in collection:
                sentences = collection[key][0]
                sent_ents = collection[key][1]

                for index in range(len(sentences)):
                    sentence_curr = sentences[index]
                    sent_ent_curr = sent_ents[index]

                    if (sent_ent_curr[1] == 'FAC') or is_road(sentence_curr, nlp):
                        result_csv.append(sentence_curr)

                    else:
                        result_csv.append(get_coords(sentence_curr))

            result_csv_t = []
            for item in result_csv:
                if item not in result_csv_t:
                    result_csv_t.append(item)

            result_csv = result_csv_t
            del result_csv_t

            with open(os.path.join(curr_path, file_target), 'a') as csvfile:
                csvwriter = csv.writer(csvfile)
                csvwriter.writerow([sno, result_csv, link, date])

            # displacy.serve(doc, style="dep", auto_select_port=True)

        except Exception as ee:
            print("error in getting article", ee)
