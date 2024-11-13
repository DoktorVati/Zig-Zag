const { Report } = require("../models")

async function createReport(postId, snitch) {
    try {
        const createdReport = Report.create({
            postId: postId,
            snitch: snitch
        });

        return createdReport;
    } catch(error) {
        throw new Error(`There was an error creating a report for POST ${postId}. ${error.message}`)
    }
}

module.exports = { createReport }