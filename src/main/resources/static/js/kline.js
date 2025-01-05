// K线图配置和初始化
let klineChart = null;
let currentTrades = []; // 保存交易记录
let activeTradeIndex = -1; // 当前活跃的交易索引
let klineData = []; // 保存K线数据

function initKlineChart() {
    const chartContainer = document.getElementById('klineChart');
    if (!chartContainer) {
        console.error('Chart container not found!');
        return;
    }

    // 检查 klinecharts 是否已加载
    if (typeof klinecharts === 'undefined') {
        console.error('KLineCharts library not loaded!');
        return false;
    }

    // 如果已经存在图表实例，先销毁
    if (klineChart) {
        try {
            // 清除容器内容
            chartContainer.innerHTML = '';
            klineChart = null;
        } catch (e) {
            console.error('Error clearing chart:', e);
        }
    }

    try {
        // 初始化图表
        klineChart = klinecharts.init(chartContainer, {
            grid: {
                show: false,
                horizontal: {
                    show: false,
                    size: 1,
                    color: '#EDEDED',
                    style: 'dashed',
                    dashedValue: [2, 2]
                },
                vertical: {
                    show: false,
                    size: 1,
                    color: '#EDEDED',
                    style: 'dashed',
                    dashedValue: [2, 2]
                }
            },
            candle: {
                type: 'candle_solid',
                bar: {
                    upColor: '#ef232a',
                    downColor: '#14b143',
                    noChangeColor: '#888888',
                    upBorderColor: '#ef232a',
                    downBorderColor: '#14b143',
                    noChangeBorderColor: '#888888',
                    upWickColor: '#ef232a',
                    downWickColor: '#14b143',
                    noChangeWickColor: '#888888'
                },
                tooltip: {
                    showRule: 'always',
                    showType: 'standard',
                    custom: [
                        { title: 'time', value: '{time}' },
                        { title: 'open', value: '{open}' },
                        { title: 'high', value: '{high}' },
                        { title: 'low', value: '{low}' },
                        { title: 'close', value: '{close}' },
                        { title: 'volume', value: '{volume}' }
                    ]
                }
            },
            xAxis: {
                show: true,
                axisLine: {
                    show: false,
                    color: '#666666',
                    size: 1
                },
                tickLine: {
                    show: false,
                    size: 1,
                    length: 3,
                    color: '#666666'
                },
                tickText: {
                    show: true,
                    color: '#666666',
                    family: 'Helvetica Neue',
                    weight: 'normal',
                    size: 12,
                    marginStart: 4,
                    marginEnd: 4
                }
            },
            yAxis: {
                show: true,
                axisLine: {
                    show: false,
                    color: '#666666',
                    size: 1
                },
                tickLine: {
                    show: false,
                    size: 1,
                    length: 3,
                    color: '#666666'
                },
                tickText: {
                    show: true,
                    color: '#666666',
                    family: 'Helvetica Neue',
                    weight: 'normal',
                    size: 12,
                    marginStart: 4,
                    marginEnd: 4
                }
            },
            separator: {
                size: 1,
                color: '#EDEDED',
                fill: true,
                activeBackgroundColor: 'rgba(33, 150, 243, 0.08)'
            },
            crosshair: {
                show: true,
                horizontal: {
                    show: false,
                    line: {
                        show: false,
                        style: 'dashed',
                        dashedValue: [4, 2],
                        size: 1,
                        color: '#666666'
                    },
                    text: {
                        show: true,
                        style: 'fill',
                        color: '#FFFFFF',
                        size: 12,
                        family: 'Helvetica Neue',
                        weight: 'normal',
                        borderStyle: 'solid',
                        borderSize: 1,
                        borderColor: '#666666',
                        borderRadius: 2,
                        borderDashedValue: [2, 2],
                        paddingLeft: 4,
                        paddingRight: 4,
                        paddingTop: 4,
                        paddingBottom: 4,
                        backgroundColor: '#666666'
                    }
                },
                vertical: {
                    show: false,
                    line: {
                        show: false,
                        style: 'dashed',
                        dashedValue: [4, 2],
                        size: 1,
                        color: '#666666'
                    },
                    text: {
                        show: true,
                        style: 'fill',
                        color: '#FFFFFF',
                        size: 12,
                        family: 'Helvetica Neue',
                        weight: 'normal',
                        borderStyle: 'solid',
                        borderSize: 1,
                        borderColor: '#666666',
                        borderRadius: 2,
                        borderDashedValue: [2, 2],
                        paddingLeft: 4,
                        paddingRight: 4,
                        paddingTop: 4,
                        paddingBottom: 4,
                        backgroundColor: '#666666'
                    }
                }
            }
        });

        // 创建成交量指标
        klineChart.createIndicator('VOL', false, {
            styles: {
                grid: {
                    show: false
                },
                bar: {
                    upColor: '#ef232a',
                    downColor: '#14b143',
                    noChangeColor: '#888888'
                }
            }
        });

        // 设置亮色背景
        chartContainer.style.backgroundColor = '#FFFFFF';

        console.log('Chart initialized successfully');
        return true;
    } catch (error) {
        console.error('Failed to initialize chart:', error);
        return false;
    }
}

function updateKlineChartWithData(data, trades = []) {
    if (!klineChart) {
        console.log('Chart not initialized, attempting to initialize...');
        if (!initKlineChart()) {
            console.error('Failed to initialize chart');
            return;
        }
    }

    if (!data || data.length === 0) {
        console.error('No data available for K-line chart');
        return;
    }

    console.log('Updating chart with data:', data.length, 'items');
    console.log('First item:', data[0]);
    console.log('Last item:', data[data.length - 1]);

    // 清空之前的交易记录和标记
    currentTrades = [];
    activeTradeIndex = -1;

    // 清空交易记录表格
    const tradeTableBody = document.querySelector('#tradeRecords tbody');
    if (tradeTableBody) {
        tradeTableBody.innerHTML = '';
    }

    // 清除图表上的所有标记
    if (klineChart) {
        klineChart.removeOverlay();
    }

    // 如果有新的交易记录，则更新
    if (trades && trades.length > 0) {
        currentTrades = trades;
        updateTradeRecords(trades);
        addTradeMarkers(trades);
    }

    klineData = data; // 保存K线数据

    try {
        // 转换K线数据格式
        const klineDataFormatted = data.map(item => ({
            timestamp: new Date(item.dateTime).getTime(),
            open: Number(item.open),
            high: Number(item.high),
            low: Number(item.low),
            close: Number(item.close),
            volume: Number(item.volume || 0)
        }));

        // 设置新数据
        klineChart.applyNewData(klineDataFormatted);

        // 添加买卖点标记
        addTradeMarkers(trades);
    } catch (error) {
        console.error('Failed to update chart data:', error);
    }
}

// 更新交易记录表格
function updateTradeRecords(trades) {
    const tbody = document.getElementById('tradeRecordsBody');
    if (!tbody) return;

    tbody.innerHTML = trades.map((trade, index) => {
        const isBuy = trade.type.toUpperCase() === 'BUY';
        const date = new Date(trade.time);
        const formattedTime = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
        const isActive = index === activeTradeIndex ? 'active' : '';
        return `
            <tr class="trade-record ${isBuy ? 'buy' : 'sell'} ${isActive}" data-index="${index}">
                <td>${formattedTime}</td>
                <td style="color: ${isBuy ? '#14b143' : '#ef232a'}">${isBuy ? '买入' : '卖出'}</td>
                <td>${Number(trade.price).toFixed(4)}</td>
                <td>${trade.quantity || '-'}</td>
            </tr>
        `;
    }).join('');

    // 添加点击事件
    const rows = tbody.getElementsByClassName('trade-record');
    Array.from(rows).forEach(row => {
        row.addEventListener('click', function () {
            const index = parseInt(this.dataset.index);
            highlightTrade(index);
        });
    });
}

// 添加交易标记
function addTradeMarkers(trades) {
    trades.forEach((trade, index) => {
        const isBuy = trade.type.toUpperCase() === 'BUY';
        const timestamp = new Date(trade.time).getTime();
        try {
            // 添加买卖标记
            klineChart.createOverlay({
                name: 'simpleAnnotation',
                points: [{ timestamp, value: Number(trade.price) }],
                styles: {
                    position: isBuy ? 'bottom' : 'top',
                    offset: [0, isBuy ? 8 : -8],
                    symbol: {
                        type: 'triangle',
                        size: 8,
                        color: isBuy ? '#14b143' : '#ef232a',
                        activeSize: 10
                    },
                    text: {
                        show: true,
                        content: isBuy ? 'B' : 'S',
                        color: '#FFFFFF',
                        backgroundColor: isBuy ? '#14b143' : '#ef232a',
                        size: 12,
                        borderRadius: 2,
                        padding: [2, 3, 2, 3],
                        offset: [0, isBuy ? 15 : -15]
                    }
                },
                onClick: () => {
                    highlightTrade(index);
                }
            });
        } catch (e) {
            console.warn('Error creating overlay:', e);
        }
    });
}

// 临时高亮显示
function temporaryHighlight(index) {
    if (!klineChart || !currentTrades || index < 0 || index >= currentTrades.length) {
        console.warn('Invalid state for temporary highlight');
        return;
    }

    const trade = currentTrades[index];
    const isBuy = trade.type.toUpperCase() === 'BUY';
    const timestamp = new Date(trade.time).getTime();

    try {
        klineChart.removeOverlay();

        // 添加买卖标记
        klineChart.createOverlay({
            name: 'simpleAnnotation',
            points: [{ timestamp, value: Number(trade.price) }],
            styles: {
                position: isBuy ? 'bottom' : 'top',
                offset: [0, isBuy ? 8 : -8],
                symbol: {
                    type: 'triangle',
                    size: 10,
                    color: isBuy ? '#14b143' : '#ef232a',
                    activeSize: 12
                },
                text: {
                    show: true,
                    content: isBuy ? 'B' : 'S',
                    color: '#FFFFFF',
                    backgroundColor: isBuy ? '#14b143' : '#ef232a',
                    size: 14,
                    borderRadius: 2,
                    padding: [2, 3, 2, 3],
                    offset: [0, isBuy ? 15 : -15]
                }
            }
        });

        // 找到对应时间点的数据索引
        const dataIndex = klineData.findIndex(item => new Date(item.dateTime).getTime() === timestamp);
        if (dataIndex !== -1) {
            // 计算显示范围
            const startIndex = Math.max(0, dataIndex - 100);
            const endIndex = Math.min(klineData.length - 1, dataIndex + 50);

            // 截取需要显示的数据
            const visibleData = klineData.slice(startIndex, endIndex + 1).map(item => ({
                timestamp: new Date(item.dateTime).getTime(),
                open: Number(item.open),
                high: Number(item.high),
                low: Number(item.low),
                close: Number(item.close),
                volume: Number(item.volume || 0)
            }));

            // 更新图表数据
            klineChart.applyNewData(visibleData);
        }
    } catch (e) {
        console.error('Error in temporary highlight:', e);
    }
}

// 高亮显示交易记录
function highlightTrade(index) {
    console.log('Highlighting trade:', index, 'Current active:', activeTradeIndex);

    if (!klineChart || !currentTrades || index < 0 || index >= currentTrades.length) {
        console.warn('Invalid state for highlight');
        return;
    }

    // 更新表格高亮
    const tbody = document.getElementById('tradeRecordsBody');
    if (tbody) {
        // 先移除所有高亮
        const rows = tbody.getElementsByClassName('trade-record');
        Array.from(rows).forEach(row => {
            row.classList.remove('active');
        });

        // 如果点击的是当前高亮的交易，取消高亮
        if (index === activeTradeIndex) {
            console.log('Canceling highlight');
            activeTradeIndex = -1;
            klineChart.removeOverlay();
            addTradeMarkers(currentTrades);
            return;
        }

        // 添加新的高亮
        if (index >= 0 && index < rows.length) {
            rows[index].classList.add('active');
            // 滚动到可见区域
            rows[index].scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    }

    // 更新图表高亮
    activeTradeIndex = index;
    const trade = currentTrades[index];
    const isBuy = trade.type.toUpperCase() === 'BUY';
    const timestamp = new Date(trade.time).getTime();

    try {
        // 找到对应时间点的数据索引
        const dataIndex = klineData.findIndex(item => new Date(item.dateTime).getTime() === timestamp);
        if (dataIndex !== -1) {
            // 设置K线柱的宽度
            const chartWidth = document.getElementById('klineChart').clientWidth;
            const defaultBarCount = Math.min(100, klineData.length); // 默认显示的K线数量
            const barSpace = Math.floor(chartWidth / defaultBarCount);
            klineChart.setBarSpace(barSpace);

            // 计算目标位置的偏移量
            const targetBarCount = Math.floor(defaultBarCount * 0.3); // 目标位置在30%处
            const rightOffset = Math.max(0, (klineData.length - dataIndex - targetBarCount) * barSpace);

            // 移除旧的标记
            klineChart.removeOverlay();

            // 添加买卖标记
            klineChart.createOverlay({
                name: 'simpleAnnotation',
                points: [{ timestamp, value: Number(trade.price) }],
                styles: {
                    position: isBuy ? 'bottom' : 'top',
                    offset: [0, isBuy ? 8 : -8],
                    symbol: {
                        type: 'triangle',
                        size: 10,
                        color: isBuy ? '#14b143' : '#ef232a',
                        activeSize: 12
                    },
                    text: {
                        show: true,
                        content: isBuy ? 'B' : 'S',
                        color: '#FFFFFF',
                        backgroundColor: isBuy ? '#14b143' : '#ef232a',
                        size: 14,
                        borderRadius: 2,
                        padding: [2, 3, 2, 3],
                        offset: [0, isBuy ? 15 : -15]
                    }
                }
            });

            // 设置右边偏移量
            klineChart.setOffsetRightDistance(rightOffset);
        }
    } catch (e) {
        console.error('Error in highlight:', e);
    }
}

// 获取K线数据
function fetchKlineData(symbol, startDate, endDate, kType = 'day', market = 'US') {
    const url = `/api/backtest/kline?symbol=${symbol}&startDate=${startDate}&endDate=${endDate}&kType=${kType}&market=${market}`;

    return fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log('Received K-line data:', data);
            return data;
        })
        .catch(error => {
            console.error('Error fetching K-line data:', error);
            throw error;
        });
}

// 响应窗口大小变化
window.addEventListener('resize', () => {
    if (klineChart) {
        klineChart.resize();
    }
});

// 页面加载完成后初始化图表
document.addEventListener('DOMContentLoaded', function () {
    initKlineChart();
}); 