// K线图配置和初始化
let klineChart = null;

function initKlineChart() {
    const chartContainer = document.getElementById('klineChart');
    if (!chartContainer) {
        console.error('Chart container not found!');
        return;
    }

    if (klineChart !== null) {
        klineChart.dispose();
    }

    klineChart = echarts.init(chartContainer);

    // 设置初始化选项
    const option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross'
            }
        },
        legend: {
            data: ['K线']
        },
        grid: {
            left: '10%',
            right: '10%',
            bottom: '15%'
        },
        xAxis: {
            type: 'category',
            scale: true,
            boundaryGap: false,
            axisLine: { onZero: false },
            splitLine: { show: false },
            splitNumber: 20,
            min: 'dataMin',
            max: 'dataMax'
        },
        yAxis: {
            scale: true,
            splitArea: {
                show: true
            }
        },
        dataZoom: [
            {
                type: 'inside',
                start: 0,
                end: 100
            },
            {
                show: true,
                type: 'slider',
                bottom: '5%',
                start: 0,
                end: 100
            }
        ],
        series: [
            {
                name: 'K线',
                type: 'candlestick',
                itemStyle: {
                    color: '#ef232a',
                    color0: '#14b143',
                    borderColor: '#ef232a',
                    borderColor0: '#14b143'
                }
            }
        ]
    };

    klineChart.setOption(option);
    klineChart.showLoading();
}

function updateKlineChartWithData(data) {
    if (!klineChart) {
        console.error('Chart not initialized');
        return;
    }

    if (!data || data.length === 0) {
        console.error('No data available for K-line chart');
        klineChart.hideLoading();
        return;
    }

    // 转换数据格式
    const klineData = data.map(item => ({
        time: item.dateTime,  // 使用原始日期格式
        open: parseFloat(item.open),
        high: parseFloat(item.high),
        low: parseFloat(item.low),
        close: parseFloat(item.close),
        volume: parseFloat(item.volume)
    }));

    // 提取日期列表
    const dates = klineData.map(item => item.time);
    // 提取K线数据
    const values = klineData.map(item => [item.open, item.close, item.low, item.high]);

    const option = {
        xAxis: {
            data: dates
        },
        series: [
            {
                name: 'K线',
                data: values
            }
        ]
    };

    klineChart.hideLoading();
    klineChart.setOption(option);
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