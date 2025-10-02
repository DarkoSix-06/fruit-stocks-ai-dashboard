import { useEffect, useMemo, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { getKpis, getSeries, postSummarize } from './api';
import {
  LineChart, Line, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import { format, subDays } from 'date-fns';
import { TrendingUp, Calendar, Loader2, Sparkles } from 'lucide-react';

function toISO(d) { return format(d, 'yyyy-MM-dd'); }

const COLORS = {
  APPLE: '#ef4444',
  ORANGE: '#f97316',
  BANANA: '#eab308',
};

/* ---------- AI summary formatting ---------- */
// Normalize AI text: convert "• " bullets to markdown list items, keep bold,
// optionally convert **Headline** to a small heading for readability.
function formatAiSummary(text = '') {
  let t = text
    .replace(/\r\n/g, '\n')
    .replace(/\t/g, '  ')
    .replace(/^\s*•\s+/gm, '- ')                   // bullets → markdown list
    .replace(/^\s*-\s*•\s+/gm, '- ')               // edge cases
    .replace(/^\s*\*\*(Headline.*?)\*\*/m, '### $1'); // nicer headline
  return t.trim();
}
/* ------------------------------------------ */

export default function App() {
  const [start, setStart] = useState(toISO(subDays(new Date(), 30)));
  const [end, setEnd] = useState(toISO(new Date()));
  const [series, setSeries] = useState([]);
  const [kpis, setKpis] = useState(null);
  const [summary, setSummary] = useState('');
  const [loading, setLoading] = useState(false);
  const [summarizing, setSummarizing] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const [s, k] = await Promise.all([getSeries(start, end), getKpis(start, end)]);
      setSeries(s);
      setKpis(k);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);            // first mount
  useEffect(() => { load(); }, [start, end]);  // when dates change

  // One row per date for the line chart
  const byDate = useMemo(() => {
    const map = {};
    for (const p of series) {
      map[p.date] = map[p.date] || { date: p.date, APPLE: 0, ORANGE: 0, BANANA: 0 };
      map[p.date][p.fruit] = p.quantity;
    }
    return Object.values(map).sort((a, b) => a.date.localeCompare(b.date));
  }, [series]);

  async function onSummarize() {
    setSummarizing(true);
    try {
      const res = await postSummarize(start, end);
      setSummary(res.summary || 'No summary available');
    } finally {
      setSummarizing(false);
    }
  }

  const formatNumber = (num) => {
    if (!num && num !== 0) return '—';
    return new Intl.NumberFormat('en-US').format(num);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 p-4 md:p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-gradient-to-br from-orange-500 to-red-500 rounded-lg shadow-lg">
              <TrendingUp className="w-6 h-6 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-slate-800">Fruit Stocks Dashboard</h1>
          </div>
          <p className="text-slate-600">Real-time inventory analytics and insights</p>
        </div>

        {/* Date Controls */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-4 mb-6">
          <div className="flex flex-wrap items-center gap-3">
            <Calendar className="w-5 h-5 text-slate-400" />
            <span className="text-sm font-medium text-slate-700">Date Range:</span>
            <input
              className="px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
              type="date"
              value={start}
              onChange={e => setStart(e.target.value)}
            />
            <span className="text-slate-400">to</span>
            <input
              className="px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
              type="date"
              value={end}
              onChange={e => setEnd(e.target.value)}
            />
            <button
              className="ml-auto px-4 py-2 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-lg font-medium hover:from-orange-600 hover:to-red-600 transition-all shadow-md hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              onClick={onSummarize}
              disabled={summarizing || loading}
            >
              {summarizing ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Generating...
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4" />
                  AI Summary
                </>
              )}
            </button>
          </div>
        </div>

        {/* Loading */}
        {loading && (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-orange-500" />
          </div>
        )}

        {!loading && (
          <>
            {/* KPI Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
              <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 hover:shadow-md transition-shadow">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-slate-600">Apple Total</span>
                  <div className="w-3 h-3 rounded-full bg-red-500"></div>
                </div>
                <div className="text-3xl font-bold text-slate-800">{formatNumber(kpis?.appleTotal)}</div>
              </div>

              <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 hover:shadow-md transition-shadow">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-slate-600">Orange Total</span>
                  <div className="w-3 h-3 rounded-full bg-orange-500"></div>
                </div>
                <div className="text-3xl font-bold text-slate-800">{formatNumber(kpis?.orangeTotal)}</div>
              </div>

              <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 hover:shadow-md transition-shadow">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-slate-600">Banana Total</span>
                  <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
                </div>
                <div className="text-3xl font-bold text-slate-800">{formatNumber(kpis?.bananaTotal)}</div>
              </div>

              <div className="bg-gradient-to-br from-orange-500 to-red-500 rounded-xl shadow-md p-6 text-white hover:shadow-lg transition-shadow">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-orange-50">Grand Total</span>
                  <TrendingUp className="w-5 h-5 text-orange-100" />
                </div>
                <div className="text-3xl font-bold">{formatNumber(kpis?.grandTotal)}</div>
              </div>
            </div>

            {/* Main Line Chart */}
            <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 mb-6">
              <h3 className="text-lg font-semibold text-slate-800 mb-4">Stock Trends</h3>
              <div style={{ height: 320 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={byDate}>
                    <XAxis dataKey="date" stroke="#94a3b8" style={{ fontSize: 12 }} />
                    <YAxis stroke="#94a3b8" style={{ fontSize: 12 }} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: 'white',
                        border: '1px solid #e2e8f0',
                        borderRadius: '8px',
                        boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)'
                      }}
                    />
                    <Legend />
                    <Line type="monotone" dataKey="APPLE" stroke={COLORS.APPLE} strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="ORANGE" stroke={COLORS.ORANGE} strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="BANANA" stroke={COLORS.BANANA} strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* AI Summary */}
            {summary && (
              <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
                <div className="flex items-center gap-2 mb-3">
                  <Sparkles className="w-5 h-5 text-orange-600" />
                  <h3 className="text-lg font-semibold text-slate-800">AI-Generated Insights</h3>
                  <div className="ml-auto flex gap-2">
                    <button
                      onClick={() => navigator.clipboard.writeText(summary)}
                      className="px-3 py-1.5 text-sm rounded-md border border-slate-300 text-slate-700 hover:bg-slate-50"
                      title="Copy"
                    >
                      Copy
                    </button>
                  </div>
                </div>

                <div className="prose prose-slate max-w-none text-slate-800">
                  <ReactMarkdown
                    remarkPlugins={[remarkGfm]}
                    components={{
                      p: ({node, ...props}) => <p className="mb-3 leading-relaxed" {...props} />,
                      ul: ({node, ...props}) => <ul className="list-disc pl-6 space-y-1 mb-3" {...props} />,
                      li: ({node, ...props}) => <li className="leading-relaxed" {...props} />,
                      h3: ({node, ...props}) => <h3 className="text-base font-semibold text-slate-900 mb-2" {...props} />,
                      strong: ({node, ...props}) => <strong className="font-semibold" {...props} />,
                      code: ({node, inline, ...props}) =>
                        inline ? <code className="px-1 py-0.5 rounded bg-slate-100" {...props} /> : <code {...props} />
                    }}
                  >
                    {formatAiSummary(summary)}
                  </ReactMarkdown>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
