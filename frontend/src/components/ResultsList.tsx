interface ResultsListProps {
    results: string[];
  }
  
  export const ResultsList = ({ results }: ResultsListProps) => {
    return (
      <div className="space-y-4">
        <div className="mb-6">
          <h2 className="text-2xl font-semibold bg-gradient-to-r from-pink-600 to-purple-800 text-transparent bg-clip-text mb-2 font-sans">Path Results</h2>
          <p className="text-sm text-gray-600 font-normal font-sans">
            Click on any article to open it in Wikipedia. Hover over links to preview the destination.
          </p>
        </div>
        {results.map((res, index) => (
          <div 
            key={index} 
            className="bg-white backdrop-blur-sm border border-blue-100 rounded-xl p-4 hover:border-blue-300 transition-all duration-200 transform hover:scale-[1.02] hover:shadow-lg"
            title={`Click to visit: ${res.split('/wiki/').pop()?.replace(/_/g, ' ')}`}
          >
            <div className="flex items-center space-x-4">
              <span className="flex-shrink-0 h-8 w-8 rounded-lg bg-gradient-to-r from-pink-600 to-purple-800 flex items-center justify-center text-white font-medium shadow-md">
                {index + 1}
              </span>
              <a
                href={res}
                target="_blank"
                rel="noopener noreferrer"
                className="group flex items-center space-x-2 text-pink-600 hover:text-purple-800 truncate transition-colors"
              >
                <span className="group-hover:underline font-normal">
                  {res.split('/wiki/').pop()?.replace(/_/g, ' ')}
                </span>
                <svg 
                  className="w-4 h-4 opacity-0 group-hover:opacity-100 transition-all duration-200" 
                  fill="none" 
                  stroke="currentColor" 
                  viewBox="0 0 24 24"
                >
                  <path 
                    strokeLinecap="round" 
                    strokeLinejoin="round" 
                    strokeWidth={2} 
                    d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" 
                  />
                </svg>
              </a>
            </div>
          </div>
        ))}
      </div>
    );
  }; 