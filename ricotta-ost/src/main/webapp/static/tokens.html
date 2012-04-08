    <table>
        <thead class="ui-widget-header">
		<tr id="tokensHeadTR">
			<th>Delete</th>
			<th>Name</th>
			<th>Description</th>
			<th>Context</th>
		</tr>
	</thead>
        <tbody id="tokensBody"></tbody>
        <tfoot>
            <tr>
                <td colspan="2"><button id="buttonCreateToken">Create new token...</button></td>
            </tr>
        </tfoot>
        
    </table>
<script type="text/javascript">

function buildTokensTable(p) {
    $.map(p.subsets, function(s, index) {
        $("#tokensHeadTR").append('<th title="' + s + '"><input type="checkbox" onClick="" /></th>');
    });
    $.map(p.tokens, function(t, index) {
        $("#tokensBody").append('<tr id="tr_' + t.id + '"></tr>');
        $("#tr_" + t.id).append('<td id="id_' + t.id + '">' + t.id + '</td>',
            '<td><input id="name_' + t.id + '" value="' + t.name + '" /></td>',
            '<td><input id="description_' + t.id + '" value="' + t.description + '" /></td>',
            '<td><select id="context_' + t.id + '" ><option value="_NO_CONTEXT_">_NO_CONTEXT_</option></select></td>');
        $.map(p.contexts, function(c, ci) {
            $("#context_" + t.id).append('<option value="' + c.name + '" ' + 
                (c.name == t.context ? 'selected="selected" ' : '') +
                '>' + c.name + '</option>');
        });
        $.map(p.subsets, function(s, si) {
            $("#tr_" + t.id).append('<td><input type="checkbox" id="subset_' + t.id + '_' + s + '"' +
                ' title="' + t.name + ':' + s + '" /></td>');
        });
        $.map(t.subsets, function(s, si) {
            $("#subset_" + t.id + "_" + s).attr("checked", "checked");
        });
    });
}

$("#headerView").click(function() {
    $("#contents").html("Loading...").load("tokens.html");
});

$(function() {
    var p = $("#headerProject").data("json");
    $("#headerView").text("Tokens").show();

    buildTokensTable(p);
});

</script>
