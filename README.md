<h1>AKSA: Apghat Kshetra Suchak App</h1>

<h3>This app is an attempt to alert the user when the user is enterring an accident-prone area so that the user takes extra care while travelling in that area. The word 'Aksa' means soul, and quite appropriately suited for the purpose it serves; an attempt at saving souls.</h3>

<h3><b>How it works:</b></h3>
<p>The application is an Android app which considers the current user location and finds out if the location lies inside any of the accident prone areas. If true, the user is alerted by the app that the user is inside an accident-prone area. The list of accident prone regions is obtained by the app by requesting to the AKSA flask server. This server, on receiving the <code>/request</code> message, returns the coordinates of the outer points which form the regions</p>

<p>This data of accidents is obtained by scraping news articles from Times of India. Data collection is done by scraping and the articles are then filtered using NLP. The first level of filtering is done on only the haedings to identify if the article does contain details of a particular accident.eg. </p>

<p>The title: <code>Biker fakes rd accident to extort Rs. 20000 and a gold ring.</code> talks about a false road accident.</p>
<p>The title: <code>270 Road accidents in the past decade in Maharashtra.</code> does talk about genuine accidents, but the time span under study is a decade.</p>
<p>The title: <code>Biker killed in a road accident.</code> talks about a true road accident and the time span is less than 7 days before the article was published.</p>

<p>As shown in the above three example titles, the first two are filtered and only the article corresponding to the third title is completely scraped. One all the sentences are extracted from the article, the word tokens related to location are identified and then the location coordinates are obtained by using the </p>
