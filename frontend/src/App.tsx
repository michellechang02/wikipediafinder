import { useState } from "react";
import axios from "axios";
import { ResultsList } from "./components/ResultsList";

function App() {
  const [results, setResults] = useState<string[]>([]);
  const [startingLink, setStartingLink] = useState<string>("");
  const [endingLink, setEndingLink] = useState<string>("");
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [nodesExplored, setNodesExplored] = useState<number>(0);
  const currentYear = new Date().getFullYear();
  const copyrightText = `© ${currentYear} All rights reserved`;

  const fetchResults = async () => {
    const endpoints = [
      "https://wikipediafinder.onrender.com/api/getResults",
      "http://localhost:8080/api/getResults"
    ];
    let lastError = null;
    setIsLoading(true);
    setNodesExplored(0);
    for (const endpoint of endpoints) {
      try {
        const response = await axios.get(endpoint, {
          params: {
            startinglink: `https://en.wikipedia.org/wiki/${startingLink.replace(/\s+/g, "_").replace(/\b\w/g, char => char.toUpperCase())}`,
            endinglink: `https://en.wikipedia.org/wiki/${endingLink.replace(/\s+/g, "_").replace(/\b\w/g, char => char.toUpperCase())}`,
          },
        });
        if (response.data.nodesExplored) {
          setNodesExplored(response.data.nodesExplored);
        }
        if (response.data.message) {
          setResults([]);
        } else {
          setResults(response.data.path || response.data);
        }
        lastError = null;
        break; // Success, exit loop
      } catch (error) {
        lastError = error;
        continue; // Try next endpoint
      }
    }
    if (lastError) {
      if (lastError instanceof Error) {
        console.error("Error fetching results:", lastError.message);
      } else {
        console.error("Unexpected error fetching results:", lastError);
      }
    }
    setIsLoading(false);
  };

  const handleQuery = async () => {
    if (!startingLink || !endingLink) {
      console.error("Both starting and ending links are required.");
      return;
    }
    try {
      await fetchResults();
    } catch (error) {
      if (error instanceof Error) {
        console.error("Unexpected error:", error.message);
      } else {
        console.error("Unexpected error:", error);
      }
    }
  };

  return (
    <div className="min-h-screen bg-purple-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        {/* Navbar */}
        <div className="flex justify-between items-center px-8 py-6 mb-8 bg-white backdrop-blur-sm rounded-2xl border border-purple-100 shadow-lg">
          <div className="text-2xl font-bold bg-gradient-to-r from-pink-600 to-purple-800 text-transparent bg-clip-text font-sans">
            mɪˈʃɛl
          </div>
          <div className="flex items-center gap-4">
            <div className="text-sm font-medium text-gray-600">{copyrightText}</div>
          </div>
        </div>

        <div className="flex flex-col lg:flex-row gap-8">
          {/* Input Section */}
          <div className="lg:w-1/2">
            <div className="bg-white backdrop-blur-sm shadow-xl rounded-3xl p-8 border border-purple-100 hover:shadow-2xl hover:scale-[1.02] transition-all duration-300">
              <div className="text-center mb-8">

                <h1 className="text-4xl font-bold bg-gradient-to-r from-pink-600 to-purple-800 text-transparent bg-clip-text mb-4 font-sans">
                  Wikipedia Path Finder
                </h1>
                <div className="mt-5">
                  <p className="text-md text-gray-600 font-normal px-3 py-1.5 border-2 border-purple-100 rounded-lg inline-block shadow-sm hover:shadow-md transition-all duration-200">University of Pennsylvania • Michelle Chang (장민지)</p>
                </div>
                <div className="mt-4">
                  <p className="text-lg text-gray-700 font-normal font-sans">
                    Discover the shortest path between Wikipedia articles
                  </p>
                </div>
                
                <div className="mt-8 mb-8">
                  <div className="h-px bg-gradient-to-r from-transparent via-purple-200 to-transparent"></div>
                </div>
              </div>

              <div className="space-y-6">
                <div className="group relative">
                  <label htmlFor="startingLink" className="block text-sm font-normal text-gray-700 mb-3 group-hover:text-pink-600 transition-colors duration-200">
                    Starting Article Topic (Case Sensitive, include Spaces)
                  </label>
                  <input
                    id="startingLink"
                    type="text"
                    value={startingLink}
                    onChange={(e) => setStartingLink(e.target.value)}
                    placeholder="e.g. South Korea"
                    className="mt-1 block w-full px-5 py-4 border-2 border-purple-100 rounded-xl focus:ring-2 focus:ring-purple-800 focus:border-purple-800 bg-white/70 shadow-sm placeholder:text-gray-400 transition-all duration-200 group-hover:bg-white/90"
                  />
                  <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity duration-200 mt-8">
                    <svg className="h-5 w-5 text-pink-600" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
                    </svg>
                  </div>
                </div>
                
                <div className="group relative">
                  <label htmlFor="endingLink" className="block text-sm font-normal text-gray-700 mb-3 group-hover:text-pink-600 transition-colors duration-200">
                    Target Article Topic (Case Sensitive, include Spaces)
                  </label>
                  <input
                    id="endingLink"
                    type="text"
                    value={endingLink}
                    onChange={(e) => setEndingLink(e.target.value)}
                    placeholder="e.g. Hangul"
                    className="mt-1 block w-full px-5 py-4 border-2 border-purple-100 rounded-xl focus:ring-2 focus:ring-purple-800 focus:border-purple-800 bg-white/70 shadow-sm placeholder:text-gray-400 transition-all duration-200 group-hover:bg-white/90"
                  />
                  <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity duration-200 mt-8">
                    <svg className="h-5 w-5 text-pink-600" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
                    </svg>
                  </div>
                </div>

                <div className="mt-6 p-6 bg-white rounded-xl border border-purple-100 shadow-md hover:shadow-lg transition-all duration-300">
                  <h3 className="text-lg font-semibold bg-gradient-to-r from-pink-600 to-purple-800 text-transparent bg-clip-text mb-3">How it Works</h3>
                  <p className="text-sm leading-relaxed text-gray-700 font-normal mb-4 font-sans">
                    This tool uses Breadth-First Search (BFS) to find the shortest path between Wikipedia articles. 
                    BFS explores the web of connections level by level, ensuring the discovered path has the minimum 
                    number of clicks needed to reach the target article.
                  </p>
                  <div className="mt-3 flex items-center space-x-2 text-sm font-normal text-gray-600">
                    <svg className="w-5 h-5 text-pink-600" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2h-1V9z" clipRule="evenodd"></path>
                    </svg>
                    <span className="text-sm font-normal text-gray-600">Search is limited to 1000 nodes to ensure quick results</span>
                  </div>
                </div>

                <button 
                  onClick={handleQuery} 
                  disabled={!startingLink || !endingLink || isLoading}
                  className="w-full mt-6 py-3.5 px-4 bg-gradient-to-r from-pink-600 to-purple-800 text-white font-medium rounded-xl hover:from-pink-700 hover:to-purple-900 transform hover:scale-[1.02] transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 shadow-lg disabled:opacity-70 disabled:cursor-not-allowed disabled:transform-none"
                >
                  {isLoading ? (
                    <span className="flex items-center justify-center">
                      <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Processing...
                    </span>
                  ) : (
                    "Find Path"
                  )}
                </button>
              </div>
            </div>
          </div>

          {/* Results Section */}
          <div className="lg:w-1/2">
            {(isLoading || results.length > 0) && (
              <div className="bg-gradient-to-br from-white/95 via-white/90 to-purple-50/90 backdrop-blur-sm shadow-xl rounded-3xl p-8 border border-purple-100 hover:shadow-2xl hover:scale-[1.02] transition-all duration-300">
                {isLoading ? (
                  <div className="text-center">
                    <p className="text-xl font-semibold bg-gradient-to-r from-pink-600 to-pink-800 text-transparent bg-clip-text">
                      Exploring Wikipedia using BFS...
                    </p>
                    <div className="mt-6 flex justify-center">
                      <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-b-4 border-purple-800"></div>
                    </div>
                  </div>
                ) : (
                  <>
                    {nodesExplored > 0 && (
                      <div className="mb-4 text-sm font-medium bg-gradient-to-r from-pink-600 to-purple-800 text-transparent bg-clip-text">
                        BFS explored {nodesExplored} nodes to find this path
                      </div>
                    )}
                    <ResultsList results={results} />
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
