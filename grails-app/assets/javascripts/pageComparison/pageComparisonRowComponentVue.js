Vue.component('comparison-component', {
    props: ['jobgroups', 'grouptopagesmap', 'comparisondata', 'index'],
    template: '#page-comparison-vue',
    mounted: function () {
        this.addListener();
    },
    methods: {
        getPages: function (group) {
            return this.grouptopagesmap[group];
        },
        addListener: function () {
            var that = this;
            if(this.index>0){
                $('#removeComparisonRow'+this.index).on('click', function() {
                    that.$parent.removeComparisonRow(that.index);
                });
            }
        }
    }
});