import { useState } from 'react';
import { useBotData } from './hooks/useBotData';
import BotControls from './components/BotControls';
import DashboardDataDisplay from './components/DashboardDataDisplay';

const App = () => {
  const [selectedInterval, setSelectedInterval] = useState('1h');
const { config, summary, history, holdings, performance, marketData, loading } = useBotData(selectedInterval);

  if (loading || !config) { 
    return (
        <div className="min-h-screen p-8 text-center text-gray-500">
            {loading ? 'Loading Dashboard...' : 'Error loading configuration.'}
        </div>
    );
  }

  const activeSymbol = config.selectedSymbol;

  return (
    <div className="min-h-screen p-4 bg-gray-100">
      <h1 className="text-3xl font-bold text-center mb-6">Automated Trading Bot</h1>
      
      <BotControls 
          currentConfig={config} 
          onIntervalChange={setSelectedInterval}
          currentInterval={selectedInterval}
      />
      
      <DashboardDataDisplay 
        summary={summary} 
        history={history} 
        holdings={holdings} 
        performance={performance}
        currentMode={config.tradingMode}
        marketData={marketData}
        symbol={activeSymbol} 
      />
    </div>
  );
};

export default App;