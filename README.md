<h1>AKSA: Apghat Kshetra Suchak App</h1>

<h3>This app is an attempt to alert the user when the user is enterring an accident-prone area so that the user takes extra care while travelling in that area. The word 'Aksa' means soul, and quite appropriately suited for the purpose it serves; an attempt at saving souls.</h3>

<h3><b>How it works:</b></h3>
<p>The application is an Android app which considers the current user location and finds out if the location lies inside any of the accident prone areas. If true, the user is alerted by the app that the user is inside an accident-prone area. The list of accident prone regions is obtained by the app by requesting to the AKSA flask server. This server, on receiving the <code>/request</code> message, returns the coordinates of the outer points which form the regions</p>

<ul>
  <li>
    <h3>Data Preprocessing</h3>
<p>The data of accidents is obtained by scraping news articles from Times of India. Data collection is done by scraping the articles and the the sentences of interests are filtered using NLP.</p>

<p><code>spacy</code> library is used in conjunction with the <code>en_core_web_md</code> model of spacy. This library is used to get the POS (Part of Speech) tag of each word token in that sentence. To find the accident-related sentences, the code finds words that suggest accidents and injuries .eg. crash, killed, injured, etc. If the the words similar to these words are found, the articles corresponding to that title is scraped. To get the constant array of words similar to the words of interest, <code>gensim</code> library is used.</p>

<p>Once the token index of all the related word tokens are identified from the article, the location coordinates are obtained by using the <code><a href="https://apihub.latlong.ai/">LatLong geocoding API</a></code>.</p>

<p>The The query for the Geocoding API is a string obtained by concatenating the found word with the children of that word with <code>dep</code> tokens among <ul><li><code>ORG</code></li><li><code>GPE</code></li><li><code>FAC</code></li><li><code>LOC</code></li><li><code>PRODUCT</code></li><li><code>NORP</code></li><li><code>PERSON</code></li></ul></p>
  </li>

  <li>
    
  </li>
</ul>
