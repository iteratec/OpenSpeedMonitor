Vue.component('comparison-component', {
    props: ['jobgroups', 'grouptopagesmap', 'comparisondata'],
    template: '#page-comparison-vue',
    methods: {
        getPages: function (group) {
            return this.grouptopagesmap[group];
        }
    }
});