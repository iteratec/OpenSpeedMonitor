<div id="editor">
    <ul>
        <li v-for="threshold in thresholds">
        <div>{{ threshold.measurand.name }}</div>
        </li>
    </ul>

    <div>
        <input type="number" min="1" :value="foo" @input="update"></input>
        {{ compiledMarkdown }}
    </div>
</div>
