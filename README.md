<h1>AKSA: Apghat Kshetra Suchak App</h1>

<h3>This app is an attempt to alert the user when the user is enterring an accident-prone area so that the user takes extra care while travelling in that area. The word 'Aksa' means soul, and quite appropriately suited for the purpose it serves; an attempt at saving souls.</h3>

<h3><b>How it works:</b></h3>
<p>The application is an Android app which considers the current user location and finds out if the location lies inside any of the accident prone areas. If true, the user is alerted by the app that the user is inside an accident-prone area. The list of accident prone regions is obtained by the app by requesting to the AKSA flask server. This server, on receiving the <code>/request</code> message, returns the coordinates of the outer points which form the regions</p>

<p>This data of accidents is obtained by scraping news articles from Times of India. Data collection is done by scraping and the articles are then filtered using NLP.</p>
