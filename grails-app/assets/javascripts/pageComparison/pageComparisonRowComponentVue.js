Vue.component('comparison-component', {
    props: ['jobgroups', 'grouptopagesmap', 'comparisondata', 'index', 'amount'],
    template: '#page-comparison-vue',
    mounted: function () {
        this.addListener();
    },
    methods: {
        getPages: function (group) {
            return this.grouptopagesmap[group];
        },
        isOnlyRow: function () {
          return this.index===0 && this.amount===1
        },
        addListener: function () {
            var that = this;
            $('#removeComparisonRow'+this.index).on('click', function() {
                that.$parent.removeComparisonRow(that.index);
            });
        }
    }
});